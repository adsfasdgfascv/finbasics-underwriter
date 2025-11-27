package com.finbasics.controller;

import com.finbasics.model.ApplicationSummary;
import com.finbasics.service.ApplicationContext;
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

import java.io.IOException;
import java.sql.SQLException;

/**
 * Main dashboard: shows pipeline KPIs and applicants list.
 * Double-click or "Open Applicant" navigates to detail + statement analysis page.
 */
public class DashboardController {

    @FXML private Label userLabel;

    @FXML private TableView<ApplicationSummary> appTable;
    @FXML private TableColumn<ApplicationSummary, String> colAppNo;
    @FXML private TableColumn<ApplicationSummary, String> colBorrower;
    @FXML private TableColumn<ApplicationSummary, String> colType;
    @FXML private TableColumn<ApplicationSummary, String> colProduct;
    @FXML private TableColumn<ApplicationSummary, String> colAmount;
    @FXML private TableColumn<ApplicationSummary, String> colStatus;
    @FXML private TableColumn<ApplicationSummary, String> colCreated;

    @FXML private Button btnNewApplication;
    @FXML private Button btnOpenApplicant;

    private final ApplicationService appService = new ApplicationService();

    @FXML
    public void initialize() {
        var user = Session.getCurrentUser();
        if (user != null) {
            userLabel.setText("Analyst: " + user.getUsername());
        } else {
            userLabel.setText("Analyst: (not logged in)");
        }

        setupTable();
        loadApplicants();

        appTable.setRowFactory(tv -> {
            TableRow<ApplicationSummary> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetailFor(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupTable() {
        colAppNo.setCellValueFactory(c -> c.getValue().applicationNumberProperty());
        colBorrower.setCellValueFactory(c -> c.getValue().borrowerNameProperty());
        colType.setCellValueFactory(c -> c.getValue().borrowerTypeProperty());
        colProduct.setCellValueFactory(c -> c.getValue().productTypeProperty());
        colAmount.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("$%,.0f", c.getValue().getRequestedAmount())));
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colCreated.setCellValueFactory(c -> c.getValue().createdAtProperty());

        colStatus.setCellFactory(col -> new TableCell<>() {
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
                    setStyle("-fx-text-fill:#27ae60; -fx-font-weight:bold;");
                } else {
                    setStyle("-fx-text-fill:#7f8c8d;");
                }
            }
        });
    }

    private void loadApplicants() {
        try {
            ObservableList<ApplicationSummary> list = appService.loadApplicationSummaries();
            appTable.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openSelectedApplicant() {
        ApplicationSummary selected = appTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openDetailFor(selected);
        }
    }

    private void openDetailFor(ApplicationSummary app) {
        try {
            ApplicationContext.setCurrentApplicationId(app.getId());
            Stage stage = (Stage) appTable.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/applicant_detail.fxml"));
            stage.setScene(new Scene(root, 1200, 720));
            stage.setTitle("Applicant " + app.getApplicationNumber());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openNewApplication() {
        try {
            Stage stage = (Stage) btnNewApplication.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/submit_application.fxml"));
            stage.setScene(new Scene(root, 1200, 720));
            stage.setTitle("New Application");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        Session.clear();
        try {
            Stage stage = (Stage) btnNewApplication.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(root, 1200, 720));
            stage.setTitle("FinBasics Underwriter - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
