package com.finbasics.controller;

import com.finbasics.model.NewApplication;
import com.finbasics.service.ApplicationException;
import com.finbasics.service.ApplicationService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SubmitApplicationController {

    @FXML private VBox viewStep1, viewStep3, formSme, formConsumer, docListContainer;
    @FXML private StackPane viewStep2;
    @FXML private HBox step1Box, step2Box, step3Box;
    @FXML private RadioButton rbSme, rbConsumer;
    @FXML private ComboBox<String> comboProduct;
    @FXML private TextField txtAmount, txtBizName, txtEin, txtNaics, txtGuarantor;
    @FXML private TextField txtConsumerName, txtSsn, txtEmployer, txtIncome;
    @FXML private DatePicker dpEstablished;
    @FXML private Label lblDocType, errorLabel;
    @FXML private Button backBtn, nextBtn;

    private int currentStep = 1;
    private ToggleGroup typeGroup;
    private final ApplicationService appService = new ApplicationService();
    // Keep track of selected files so we can validate later if needed
    private final Map<String, File> selectedDocuments = new HashMap<>();

    @FXML
    public void initialize() {
        typeGroup = new ToggleGroup();
        rbSme.setToggleGroup(typeGroup);
        rbConsumer.setToggleGroup(typeGroup);
        rbSme.setSelected(true);

        typeGroup.selectedToggleProperty().addListener((obs, o, n) -> updateProductList());
        updateProductList();
        updateView();
    }

    private void updateProductList() {
        comboProduct.getItems().clear();
        if (rbSme.isSelected()) {
            comboProduct.getItems().addAll("SME Term Loan", "Line of Credit", "CRE Mortgage", "Equipment Lease");
        } else {
            comboProduct.getItems().addAll("Personal Loan", "Auto Loan", "Home Equity (HELOC)", "Mortgage");
        }
        comboProduct.getSelectionModel().selectFirst();
    }

    private void updateView() {
        viewStep1.setVisible(currentStep == 1);
        viewStep1.setManaged(currentStep == 1);
        
        viewStep2.setVisible(currentStep == 2);
        viewStep2.setManaged(currentStep == 2);
        
        viewStep3.setVisible(currentStep == 3);
        viewStep3.setManaged(currentStep == 3);

        if (currentStep == 2) {
            boolean isSme = rbSme.isSelected();
            formSme.setVisible(isSme);
            formSme.setManaged(isSme);
            formConsumer.setVisible(!isSme);
            formConsumer.setManaged(!isSme);
        }

        if (currentStep == 3) {
            buildDocChecklist();
        }

        backBtn.setDisable(currentStep == 1);
        nextBtn.setText(currentStep == 3 ? "Submit Application" : "Next \u2192");

        highlightStep(step1Box, currentStep >= 1);
        highlightStep(step2Box, currentStep >= 2);
        highlightStep(step3Box, currentStep >= 3);
    }

    private void highlightStep(HBox box, boolean active) {
        if (active) {
            box.setStyle("-fx-padding:10; -fx-background-color:#e8f6f3; -fx-border-color:#27ae60; -fx-border-width:0 0 0 4;");
        } else {
            box.setStyle("-fx-padding:10; -fx-background-color:transparent;");
        }
    }

    private void buildDocChecklist() {
        docListContainer.getChildren().clear();
        selectedDocuments.clear();
        boolean isSme = rbSme.isSelected();
        lblDocType.setText(isSme ? "SME Standard Pack" : "Consumer Standard Pack");

        String[] items = isSme ? new String[]{
                "Business Tax Returns (2 Yrs)", "YTD P&L and Balance Sheet",
                "Bank Statements (3 Months)", "Articles of Incorporation"
        } : new String[]{
                "Personal Tax Returns (2 Yrs)", "Recent Pay Stubs (30 Days)",
                "Government ID Copy", "Bank Statements (2 Months)"
        };

        for (String docName : items) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding:12; -fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:6; -fx-effect:dropshadow(three-pass-box, rgba(0,0,0,0.05), 3, 0, 0, 1);");
            
            CheckBox cb = new CheckBox();
            cb.setDisable(true); // Checkbox is controlled by file selection
            
            VBox labelBox = new VBox(2);
            Label lbl = new Label(docName);
            lbl.setStyle("-fx-font-weight:bold; -fx-text-fill:#2c3e50;");
            Label subLbl = new Label("Required document");
            subLbl.setStyle("-fx-font-size:10; -fx-text-fill:#7f8c8d;");
            labelBox.getChildren().addAll(lbl, subLbl);
            
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button btn = new Button("Select File...");
            btn.setStyle("-fx-background-color:#ecf0f1; -fx-text-fill:#2c3e50; -fx-cursor:hand;");

            btn.setOnAction(e -> {
                FileChooser fc = new FileChooser();
                fc.setTitle("Attach " + docName);
                // FIX: Add filters for documents AND images for IDs
                fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Documents & Images", "*.pdf", "*.csv", "*.xlsx", "*.jpg", "*.jpeg", "*.png"),
                    new FileChooser.ExtensionFilter("Images Only", "*.jpg", "*.jpeg", "*.png"),
                    new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"),
                    new FileChooser.ExtensionFilter("Data Files (CSV/Excel)", "*.csv", "*.xlsx")
                );

                File f = fc.showOpenDialog(btn.getScene().getWindow());
                if (f != null) {
                    selectedDocuments.put(docName, f);
                    btn.setText(f.getName());
                    btn.setStyle("-fx-background-color:#d5f5e3; -fx-text-fill:#27ae60; -fx-border-color:#27ae60;");
                    cb.setSelected(true);
                    subLbl.setText("Attached: " + f.length() / 1024 + " KB");
                }
            });

            row.getChildren().addAll(cb, labelBox, spacer, btn);
            docListContainer.getChildren().add(row);
        }
    }

    @FXML
    private void goNext() {
        errorLabel.setText("");
        if (currentStep == 1 && !validateStep1()) return;
        if (currentStep == 2 && !validateStep2()) return;

        if (currentStep < 3) {
            currentStep++;
            updateView();
        } else {
            // Optional: Validate that at least one document is attached before submission
            if (selectedDocuments.isEmpty()) {
                 errorLabel.setText("Please attach at least one required document to proceed.");
                 return;
            }
            submitApplication();
        }
    }

    @FXML private void goBack() { errorLabel.setText(""); if (currentStep > 1) { currentStep--; updateView(); } }

    private boolean validateStep1() {
        if (txtAmount.getText().isBlank()) { errorLabel.setText("Requested amount is required."); return false; }
        try {
            double amt = parseMoney(txtAmount.getText());
             if (amt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) { errorLabel.setText("Invalid amount format. Enter a positive number."); return false; }
        return true;
    }

    private boolean validateStep2() {
        if (rbSme.isSelected()) {
            if (txtBizName.getText().isBlank()) { errorLabel.setText("Business name is required."); return false; }
             if (txtEin.getText().isBlank()) { errorLabel.setText("EIN is required for business applications."); return false; }
        } else {
            if (txtConsumerName.getText().isBlank()) { errorLabel.setText("Applicant name is required."); return false; }
            if (txtSsn.getText().isBlank()) { errorLabel.setText("SSN is required for personal applications."); return false; }
            if (!txtIncome.getText().isBlank()) {
                try { parseMoney(txtIncome.getText()); } catch (NumberFormatException ex) { errorLabel.setText("Income must be numeric."); return false; }
            }
        }
        return true;
    }

    private void submitApplication() {
        try {
            NewApplication dto = buildDto();
            // In a real app, we would upload the 'selectedDocuments' here along with the DTO.
            // For this prototype, we just simulate the metadata submission.
            int appId = appService.submitNewApplication(dto);
            
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Application Submitted");
            a.setHeaderText("Success!");
            a.setContentText("Application ID " + appId + " has been submitted and sent to underwriting for analysis.");
            a.showAndWait();
            goHome();
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Submission Error: " + ex.getMessage());
        }
    }

    private NewApplication buildDto() {
        boolean isSme = rbSme.isSelected();
        NewApplication dto = new NewApplication();
        dto.setBorrowerType(isSme ? "SME" : "CONSUMER");
        dto.setProductType(comboProduct.getSelectionModel().getSelectedItem());
        dto.setRequestedAmount(parseMoney(txtAmount.getText()));

        if (isSme) {
            dto.setBusinessName(txtBizName.getText().trim());
            dto.setEin(txtEin.getText().trim());
            dto.setNaicsCode(txtNaics.getText().trim());
            dto.setDateEstablishedIso(dpEstablished.getValue() != null ? dpEstablished.getValue().toString() : null);
            dto.setGuarantorName(txtGuarantor.getText().trim());
            dto.setBorrowerName(dto.getBusinessName());
            dto.setBorrowerIdNumber(dto.getEin());
        } else {
            dto.setConsumerName(txtConsumerName.getText().trim());
            dto.setSsn(txtSsn.getText().trim());
            dto.setEmployer(txtEmployer.getText().trim());
            if (!txtIncome.getText().isBlank()) dto.setAnnualIncome(parseMoney(txtIncome.getText()));
            dto.setBorrowerName(dto.getConsumerName());
            dto.setBorrowerIdNumber(dto.getSsn());
        }
        return dto;
    }

    private double parseMoney(String txt) {
        if (txt == null || txt.isBlank()) return 0.0;
        return Double.parseDouble(txt.replaceAll("[$,]", "").trim());
    }
    
    @FXML private void cancel() { goHome(); }
    
    private void goHome() {
        try {
            Stage stage = (Stage) nextBtn.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
            stage.setScene(new Scene(root, 1200, 720));
        } catch (IOException e) { e.printStackTrace(); }
    }
}