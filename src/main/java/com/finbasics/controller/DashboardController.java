package com.finbasics.controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.finbasics.service.Session;

public class DashboardController {

    @FXML private Label userLabel;
    @FXML private TableView<ApplicationModel> appTable;
    @FXML private TableColumn<ApplicationModel, String> colId;
    @FXML private TableColumn<ApplicationModel, String> colEntity;
    @FXML private TableColumn<ApplicationModel, String> colProduct;
    @FXML private TableColumn<ApplicationModel, String> colAmount;
    @FXML private TableColumn<ApplicationModel, String> colStatus;
    @FXML private TableColumn<ApplicationModel, String> colSla;
    @FXML private TableColumn<ApplicationModel, Void> colAction; // For buttons
    
    @FXML private ListView<String> taskListView;

    @FXML
    public void initialize() {
        // 1. Initialize User Context
        // In real app, get this from Session.currentUser
        userLabel.setText("Analyst: Admin User");

        // 2. Setup Smart Queue Columns
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colEntity.setCellValueFactory(cell -> cell.getValue().entityProperty());
        colProduct.setCellValueFactory(cell -> cell.getValue().productProperty());
        colAmount.setCellValueFactory(cell -> cell.getValue().amountProperty());
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
        colSla.setCellValueFactory(cell -> cell.getValue().slaProperty());
        
        // Custom Rendering for "Micro-States" (Color coding)
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Visual cues for states
                    if (item.contains("Review")) setStyle("-fx-text-fill: #d35400; -fx-font-weight: bold;"); // Amber
                    else if (item.contains("Docs")) setStyle("-fx-text-fill: #7f8c8d;"); // Grey
                    else if (item.contains("Approved")) setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Green
                }
            }
        });

        // 3. Load Dummy Data (Simulating Database Fetch)
        ObservableList<ApplicationModel> data = FXCollections.observableArrayList(
            new ApplicationModel("A-101", "Acme Logistics LLC", "SME Term", "$1,200,000", "Analyst Review", "4h left"),
            new ApplicationModel("A-102", "Beta Retailers Inc", "Line of Credit", "$500,000", "Pending Docs", "2 days"),
            new ApplicationModel("A-103", "John Doe Properties", "CRE Mortgage", "$3,500,000", "Structuring", "1 day")
        );
        appTable.setItems(data);

        // 4. Setup Task Inbox (The "SLA Monitor")
        ObservableList<String> tasks = FXCollections.observableArrayList(
            "⚠ Review Tax Returns for Acme Logistics (SLA Warning)",
            "• Clarify ownership structure for Beta Retailers",
            "• Validate collateral appraisal for John Doe"
        );
        taskListView.setItems(tasks);
    }

    @FXML
    public void openNewApplication(ActionEvent event) {
        try {
            Stage stage = (Stage) appTable.getScene().getWindow();
            // Load the Wizard Framework
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/submit_application.fxml"));
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("FinBasics - New Application Wizard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout(ActionEvent event) {
        Session.clear();
        try {
            Stage stage = (Stage) appTable.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(root, 1200, 720));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // --- Internal Model for TableView (Simulation) ---
    public static class ApplicationModel {
        private final SimpleStringProperty id;
        private final SimpleStringProperty entity;
        private final SimpleStringProperty product;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty status;
        private final SimpleStringProperty sla;

        public ApplicationModel(String id, String entity, String product, String amount, String status, String sla) {
            this.id = new SimpleStringProperty(id);
            this.entity = new SimpleStringProperty(entity);
            this.product = new SimpleStringProperty(product);
            this.amount = new SimpleStringProperty(amount);
            this.status = new SimpleStringProperty(status);
            this.sla = new SimpleStringProperty(sla);
        }
        
        public StringProperty idProperty() { return id; }
        public StringProperty entityProperty() { return entity; }
        public StringProperty productProperty() { return product; }
        public StringProperty amountProperty() { return amount; }
        public StringProperty statusProperty() { return status; }
        public StringProperty slaProperty() { return sla; }
    }
}