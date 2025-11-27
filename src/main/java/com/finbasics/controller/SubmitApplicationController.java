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
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.IOException;

/**
 * Three-step submission wizard:
 * 1) Borrower type, product, amount
 * 2) SME vs Consumer info
 * 3) Document checklist
 */
public class SubmitApplicationController {

    // Step containers
    @FXML private VBox viewStep1;
    @FXML private VBox viewStep3;
    @FXML private StackPane viewStep2;

    @FXML private VBox formSme;
    @FXML private VBox formConsumer;

    @FXML private HBox step1Box;
    @FXML private HBox step2Box;
    @FXML private HBox step3Box;

    // Step 1
    @FXML private RadioButton rbSme;
    @FXML private RadioButton rbConsumer;
    @FXML private ComboBox<String> comboProduct;
    @FXML private TextField txtAmount;

    // Step 2 SME
    @FXML private TextField txtBizName;
    @FXML private TextField txtEin;
    @FXML private TextField txtNaics;
    @FXML private DatePicker dpEstablished;
    @FXML private TextField txtGuarantor;

    // Step 2 Consumer
    @FXML private TextField txtConsumerName;
    @FXML private TextField txtSsn;
    @FXML private TextField txtEmployer;
    @FXML private TextField txtIncome;

    // Step 3
    @FXML private VBox docListContainer;
    @FXML private Label lblDocType;

    // Navigation
    @FXML private Button backBtn;
    @FXML private Button nextBtn;
    @FXML private Label errorLabel;

    private int currentStep = 1;
    private ToggleGroup typeGroup;

    private final ApplicationService appService = new ApplicationService();

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
        viewStep2.setVisible(currentStep == 2);
        viewStep3.setVisible(currentStep == 3);

        if (currentStep == 2) {
            boolean isSme = rbSme.isSelected();
            formSme.setVisible(isSme);
            formConsumer.setVisible(!isSme);
        }

        if (currentStep == 3) {
            buildDocChecklist();
        }

        backBtn.setDisable(currentStep == 1);
        nextBtn.setText(currentStep == 3 ? "Create Application" : "Next →");

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
        boolean isSme = rbSme.isSelected();

        lblDocType.setText(isSme ? "SME Standard Pack" : "Consumer Standard Pack");

        String[] items;
        if (isSme) {
            items = new String[]{
                    "Business Tax Returns (Last 2 Years)",
                    "YTD Profit & Loss Statement",
                    "Current Balance Sheet",
                    "Bank Statements (Last 3 Months)",
                    "Articles of Incorporation"
            };
        } else {
            items = new String[]{
                    "Personal Tax Returns (Last 2 Years)",
                    "Recent Pay Stubs (Last 30 Days)",
                    "W-2 Forms",
                    "Government ID Copy",
                    "Bank Statements (Last 2 Months)"
            };
        }

        for (String text : items) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding:8; -fx-background-color:#f8f9fa; -fx-border-color:#e5e7eb; -fx-border-radius:4;");
            CheckBox cb = new CheckBox();
            Label lbl = new Label(text);
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button btn = new Button("Select File...");
            row.getChildren().addAll(cb, lbl, spacer, btn);
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

    private boolean validateStep1() {
        if (txtAmount.getText().isBlank()) {
            errorLabel.setText("Requested amount is required.");
            return false;
        }
        try {
            parseMoney(txtAmount.getText());
        } catch (NumberFormatException ex) {
            errorLabel.setText("Invalid amount format.");
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (rbSme.isSelected()) {
            if (txtBizName.getText().isBlank()) {
                errorLabel.setText("Business name is required for SME borrowers.");
                return false;
            }
        } else {
            if (txtConsumerName.getText().isBlank()) {
                errorLabel.setText("Applicant name is required.");
                return false;
            }
            if (!txtIncome.getText().isBlank()) {
                try {
                    parseMoney(txtIncome.getText());
                } catch (NumberFormatException ex) {
                    errorLabel.setText("Annual income must be numeric.");
                    return false;
                }
            }
        }
        return true;
    }

    private void submitApplication() {
        try {
            NewApplication dto = buildDto();
            int appId = appService.submitNewApplication(dto);

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Application Created");
            a.setHeaderText("Application successfully created and analyzed.");
            a.setContentText("Application ID: " + appId);
            a.showAndWait();

            goHome();
        } catch (ApplicationException ex) {
            ex.printStackTrace();
            errorLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Unexpected error while creating application.");
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
            dto.setDateEstablishedIso(dpEstablished.getValue() != null
                    ? dpEstablished.getValue().toString() : null);
            dto.setGuarantorName(txtGuarantor.getText().trim());

            dto.setBorrowerName(dto.getBusinessName());
            dto.setBorrowerIdNumber(dto.getEin() != null && !dto.getEin().isBlank()
                    ? "EIN " + dto.getEin()
                    : "SME-BORROWER");
        } else {
            dto.setConsumerName(txtConsumerName.getText().trim());
            dto.setSsn(txtSsn.getText().trim());
            dto.setEmployer(txtEmployer.getText().trim());
            if (!txtIncome.getText().isBlank()) {
                dto.setAnnualIncome(parseMoney(txtIncome.getText()));
            }

            dto.setBorrowerName(dto.getConsumerName());
            dto.setBorrowerIdNumber(dto.getSsn() != null && !dto.getSsn().isBlank()
                    ? "SSN " + dto.getSsn()
                    : "CONSUMER-BORROWER");
        }

        return dto;
    }

    private double parseMoney(String txt) {
        String cleaned = txt.replace(",", "").replace("$", "").trim();
        return Double.parseDouble(cleaned);
    }

    @FXML
    private void cancel() {
        goHome();
    }

    private void goHome() {
        try {
            Stage stage = (Stage) nextBtn.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
            stage.setScene(new Scene(root, 1200, 720));
            stage.setTitle("FinBasics Underwriter - Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
