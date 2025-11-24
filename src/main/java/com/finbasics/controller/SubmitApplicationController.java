package com.finbasics.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
public class SubmitApplicationController {

    // --- Navigation UI ---
    @FXML private VBox viewStep1, viewStep3, docListContainer;
    @FXML private StackPane viewStep2;
    @FXML private VBox formSme, formConsumer;
    @FXML private HBox step1Box, step2Box, step3Box;
    @FXML private Button backBtn, nextBtn;
    @FXML private Label errorLabel, lblDocType;

    // --- Data Inputs ---
    @FXML private RadioButton rbSme, rbConsumer;
    @FXML private ComboBox<String> comboProduct;
    @FXML private TextField txtAmount;
    
    // SME Inputs
    @FXML private TextField txtBizName, txtEin, txtNaics, txtGuarantor;
    @FXML private DatePicker dpEstablished;
    
    // Consumer Inputs
    @FXML private TextField txtConsumerName, txtSsn, txtEmployer, txtIncome;

    private int currentStep = 1;
    private ToggleGroup typeGroup;

    @FXML
    public void initialize() {
        // 1. Setup Toggle Group for SME vs Consumer
        typeGroup = new ToggleGroup();
        rbSme.setToggleGroup(typeGroup);
        rbConsumer.setToggleGroup(typeGroup);

        // 2. Listen for Type Change to update Product List
        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateProductList());
        updateProductList(); // Init defaults
    }

    private void updateProductList() {
        comboProduct.getItems().clear();
        if (rbSme.isSelected()) {
            comboProduct.getItems().addAll(
                "SME Term Loan", "Line of Credit", "CRE Mortgage", "Equipment Lease"
            );
        } else {
            comboProduct.getItems().addAll(
                "Personal Loan", "Auto Loan", "Home Equity (HELOC)", "Mortgage"
            );
        }
        comboProduct.getSelectionModel().selectFirst();
    }

    @FXML
    private void goNext() {
        errorLabel.setText(""); // Clear errors

        // Validation logic could go here
        if (currentStep == 1 && txtAmount.getText().isEmpty()) {
            errorLabel.setText("Please enter a requested amount.");
            return;
        }

        if (currentStep < 3) {
            currentStep++;
            updateView();
        } else {
            submitApplication();
        }
    }

    @FXML
    private void goBack() {
        errorLabel.setText("");
        if (currentStep > 1) {
            currentStep--;
            updateView();
        }
    }

    private void updateView() {
        // 1. Manage Visibility
        viewStep1.setVisible(currentStep == 1);
        viewStep2.setVisible(currentStep == 2);
        viewStep3.setVisible(currentStep == 3);

        // 2. Step 2 Logic: Show correct form (SME vs Consumer)
        if (currentStep == 2) {
            boolean isSme = rbSme.isSelected();
            formSme.setVisible(isSme);
            formConsumer.setVisible(!isSme);
        }

        // 3. Step 3 Logic: Generate correct checklist
        if (currentStep == 3) {
            generateDocChecklist();
        }

        // 4. Update Navigation Buttons
        backBtn.setDisable(currentStep == 1);
        nextBtn.setText(currentStep == 3 ? "Create Application" : "Next Step →");

        // 5. Update Sidebar Indicators
        highlightStep(step1Box, currentStep >= 1);
        highlightStep(step2Box, currentStep >= 2);
        highlightStep(step3Box, currentStep >= 3);
    }

    /**
     * Dynamically builds the document checklist based on Loan Type.
     * This fulfills the requirement for "Policy-Driven Checklists".
     */
    private void generateDocChecklist() {
        docListContainer.getChildren().clear();
        boolean isSme = rbSme.isSelected();
        
        lblDocType.setText(isSme ? "(SME Standard Pack)" : "(Consumer Standard Pack)");
        lblDocType.setStyle(isSme ? "-fx-background-color: #d6eaf8; -fx-text-fill: #2980b9;" : "-fx-background-color: #d5f5e3; -fx-text-fill: #27ae60;");

        String[] requirements;
        if (isSme) {
            requirements = new String[] {
                "Business Tax Returns (Last 2 Years)",
                "YTD Profit & Loss Statement",
                "Balance Sheet (Current)",
                "Business Bank Statements (Last 3 Months)",
                "Articles of Incorporation / Org"
            };
        } else {
            requirements = new String[] {
                "Personal Tax Returns (Last 2 Years)",
                "Pay Stubs (Last 30 Days)",
                "W-2 Forms (Last 2 Years)",
                "Copy of Govt ID (Driver's License)",
                "Personal Bank Statements (Last 2 Months)"
            };
        }

        // Create UI row for each requirement
        for (String req : requirements) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-radius: 4;");
            
            CheckBox cb = new CheckBox(); // Just for visual tracking
            Label lbl = new Label(req);
            lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button uploadBtn = new Button("Select File...");
            uploadBtn.setStyle("-fx-font-size: 11;");

            row.getChildren().addAll(cb, lbl, spacer, uploadBtn);
            docListContainer.getChildren().add(row);
        }
    }

    private void highlightStep(HBox box, boolean active) {
        if (active) {
            box.setStyle("-fx-padding: 15 20; -fx-background-color: #e8f6f3; -fx-border-color: #27ae60; -fx-border-width: 0 0 0 4;");
        } else {
            box.setStyle("-fx-padding: 15 20; -fx-background-color: transparent;");
        }
    }

    @FXML
    private void cancel() {
        returnToDashboard();
    }

    private void submitApplication() {
        // TODO: Map to Database (SQLite)
        // 1. Create 'Borrower' record
        // 2. Create 'Application' record (Status = INTAKE)
        // 3. Create 'Task' records based on missing docs
        
        System.out.println("DEBUG: Submitting Application...");
        System.out.println("Type: " + (rbSme.isSelected() ? "SME" : "Consumer"));
        System.out.println("Amount: " + txtAmount.getText());
        
        // Return to Hub/Dashboard
        returnToDashboard();
    }

    private void returnToDashboard() {
        try {
            Stage stage = (Stage) nextBtn.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
            stage.setScene(new Scene(root, 1200, 720));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}