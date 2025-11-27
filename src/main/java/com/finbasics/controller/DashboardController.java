package com.finbasics.controller;

import com.finbasics.model.ApplicationSummary;
import com.finbasics.service.ApplicationService;
import com.finbasics.service.Session;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label userLabel;

    // Applicants list (right side in wireframe)
    @FXML private TableView<ApplicationSummary> appTable;
    @FXML private TableColumn<ApplicationSummary, String> colAppNo;
    @FXML private TableColumn<ApplicationSummary, String> colBorrower;
    @FXML private TableColumn<ApplicationSummary, String> colProduct;
    @FXML private TableColumn<ApplicationSummary, String> colAmount;
    @FXML private TableColumn<ApplicationSummary, String> colStatus;
    @FXML private TableColumn<ApplicationSummary, String> colCreated;

    // Detail + Statement Analysis panel
    @FXML private Label lblApplicantName;
    @FXML private Label lblApplicantProduct;
    @FXML private Label lblApplicantAmount;
    @FXML private Label lblApplicantStatus;
    @FXML private Label lblApplicantCreated;

    @FXML private Label lblDscr;
    @FXML private Label lblCurrentRatio;
    @FXML private Label lblDebtToEquity;
    @FXML private Label lblNetMargin;

    @FXML private Button btnNewApplication;

    private final ApplicationService appService = new ApplicationService();

    @FXML
    public void initialize() {
        var user = Session.getCurrentUser();
        if (user != null) {
            userLabel.setText("Analyst: " + user.getusername());
        } else {
            userLabel.setText("Analyst: (not logged in)");
        }

        setupTable();
        loadApplicants();

        appTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> showSummary(newSel));
    }

    private void setupTable() {
        colAppNo.setCellValueFactory(cell -> cell.getValue().applicationNumberProperty());
        colBorrower.setCellValueFactory(cell -> cell.getValue().borrowerNameProperty());
        colProduct.setCellValueFactory(cell -> cell.getValue().productTypeProperty());
        colAmount.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("$%,.0f", cell.getValue().getRequestedAmount())));
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
        colCreated.setCellValueFactory(cell -> cell.getValue().createdAtProperty());

        // Simple visual cue for status
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if (item.startsWith("ANALYZED")) {
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                } else if (item.startsWith("INTAKE") || item.startsWith("REVIEW")) {
                    setStyle("-fx-text-fill: #d35400; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadApplicants() {
        try {
            ObservableList<ApplicationSummary> data = appService.loadApplicationSummaries();
            appTable.setItems(data);
            if (!data.isEmpty()) {
                appTable.getSelectionModel().selectFirst();
                showSummary(data.getFirst());
            } else {
                clearSummary();
            }
        } catch (Exception e) {
            e.printStackTrace();
            clearSummary();
        }
    }

    private void showSummary(ApplicationSummary s) {
        if (s == null) {
            clearSummary();
            return;
        }

        lblApplicantName.setText(s.getBorrowerName());
        lblApplicantProduct.setText(s.getProductType());
        lblApplicantAmount.setText(String.format("$%,.0f", s.getRequestedAmount()));
        lblApplicantStatus.setText(s.getStatus());
        lblApplicantCreated.setText(s.getCreatedAt());

        lblDscr.setText(formatRatio(s.getDscr()));
        lblCurrentRatio.setText(formatRatio(s.getCurrentRatio()));
        lblDebtToEquity.setText(formatRatio(s.getDebtToEquity()));
        lblNetMargin.setText(String.format("%.1f%%", s.getNetMargin() * 100.0));
    }

    private void clearSummary() {
        lblApplicantName.setText("No applicant selected");
        lblApplicantProduct.setText("-");
        lblApplicantAmount.setText("-");
        lblApplicantStatus.setText("-");
        lblApplicantCreated.setText("-");

        lblDscr.setText("-");
        lblCurrentRatio.setText("-");
        lblDebtToEquity.setText("-");
        lblNetMargin.setText("-");
    }

    private String formatRatio(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value == 0.0) {
            return "-";
        }
        return String.format("%.2f×", value);
    }

    @FXML
    public void openNewApplication() {
        try {
            Stage stage = (Stage) btnNewApplication.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/submit_application.fxml"));
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("FinBasics - New Application");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout() {
        com.finbasics.service.Session.clear();
        try {
            Stage stage = (Stage) btnNewApplication.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(root, 1200, 720));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
