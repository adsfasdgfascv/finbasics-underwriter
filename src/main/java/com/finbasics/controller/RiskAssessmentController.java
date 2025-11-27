package com.finbasics.controller;

import com.finbasics.model.ApplicationSummary;
import com.finbasics.model.StatementAnalysis;
import com.finbasics.persistence.ApplicationRepository;
import com.finbasics.persistence.StatementAnalysisRepository;
import com.finbasics.service.ApplicationContext;
import com.finbasics.service.FinancialCalculator;
import com.finbasics.service.FinancialCalculator.Evaluation;
import com.finbasics.service.FinancialCalculator.Status;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiskAssessmentController {

    @FXML private Label lblBorrowerName, lblAppNumber, lblProduct, lblAmount;
    @FXML private Label lblRiskGrade, lblRecommendation;
    
    @FXML private VBox eligibilityContainer;
    @FXML private VBox flagsContainer;
    @FXML private TextArea txtComments;

    private final ApplicationRepository appRepo = new ApplicationRepository();
    private final StatementAnalysisRepository saRepo = new StatementAnalysisRepository();
    private Integer currentAppId;

    @FXML
    public void initialize() {
        currentAppId = ApplicationContext.getCurrentApplicationId();
        if (currentAppId == null) return;

        try {
            ApplicationSummary summary = appRepo.findHeader(currentAppId);
            StatementAnalysis analysis = saRepo.findByApplicationId(currentAppId);

            if (summary != null && analysis != null) {
                populateHeader(summary);
                runAssessment(analysis);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateHeader(ApplicationSummary s) {
        lblBorrowerName.setText(s.getBorrowerName());
        lblAppNumber.setText(s.getApplicationNumber());
        lblProduct.setText(s.getProductType());
        lblAmount.setText(String.format("$%,.0f", s.getRequestedAmount()));
    }

    private void runAssessment(StatementAnalysis sa) {
        // 1. Credit Tier
        String tier = FinancialCalculator.calculateOverallRiskGrade(sa);
        lblRiskGrade.setText(tier);
        updateRecommendation(tier);

        // 2. Eligibility Checks & Flags
        eligibilityContainer.getChildren().clear();
        flagsContainer.getChildren().clear();

        if ("SME".equalsIgnoreCase(sa.getBorrowerType())) {
            runSmeChecks(sa);
        } else {
            runConsumerChecks(sa);
        }
        
        if (flagsContainer.getChildren().isEmpty()) {
            Label safe = new Label("No significant risk flags identified.");
            safe.setStyle("-fx-text-fill:#27ae60; -fx-font-style:italic;");
            flagsContainer.getChildren().add(safe);
        }
    }

    private void updateRecommendation(String tier) {
        if (tier.contains("Tier 1")) {
            lblRiskGrade.setStyle("-fx-font-size:36; -fx-font-weight:800; -fx-text-fill:#27ae60;"); // Green
            lblRecommendation.setText("Strong profile. Recommended for Approval.");
        } else if (tier.contains("Tier 2")) {
            lblRiskGrade.setStyle("-fx-font-size:36; -fx-font-weight:800; -fx-text-fill:#f39c12;"); // Orange
            lblRecommendation.setText("Moderate risk. Approve with conditions.");
        } else {
            lblRiskGrade.setStyle("-fx-font-size:36; -fx-font-weight:800; -fx-text-fill:#c0392b;"); // Red
            lblRecommendation.setText("High risk. Decline recommended.");
        }
    }

    private void runSmeChecks(StatementAnalysis sa) {
        // Eligibility
        addCheck("Minimum DSCR >= 1.25x", sa.getDscr() >= 1.25);
        addCheck("Positive Net Income", sa.getNetIncome() > 0);
        addCheck("Current Ratio >= 1.0", sa.getCurrentRatio() >= 1.0);

        // Flags
        checkFlag(FinancialCalculator.evaluateSmeDscr(sa.getDscr()));
        checkFlag(FinancialCalculator.evaluateSmeLeverage(sa.getDebtToEquity()));
        checkFlag(FinancialCalculator.evaluateSmeProfitability(sa.getNetMargin()));
        if (sa.getDso() > 90) addFlag("Efficiency Warning", "DSO is high (" + String.format("%.0f", sa.getDso()) + " days), indicating slow collections.");
    }

    private void runConsumerChecks(StatementAnalysis sa) {
        // Eligibility
        addCheck("Credit Score >= 640", sa.getCreditScore() >= 640);
        addCheck("DTI Ratio <= 43%", sa.getDti() <= 0.43);
        if (sa.getLtv() > 0) addCheck("LTV <= 90%", sa.getLtv() <= 0.90);

        // Flags
        checkFlag(FinancialCalculator.evaluateConsumerDti(sa.getDti()));
        checkFlag(FinancialCalculator.evaluateConsumerCreditScore(sa.getCreditScore()));
        if (sa.getLtv() > 0) checkFlag(FinancialCalculator.evaluateConsumerLtv(sa.getLtv()));
    }

    private void addCheck(String label, boolean pass) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(pass ? "\u2714" : "\u2716"); // Check or X
        icon.setStyle(pass ? "-fx-text-fill:#27ae60; -fx-font-weight:bold;" : "-fx-text-fill:#c0392b; -fx-font-weight:bold;");
        Label text = new Label(label);
        text.setStyle(pass ? "-fx-text-fill:#2c3e50;" : "-fx-text-fill:#7f8c8d; -fx-strikethrough:true;");
        row.getChildren().addAll(icon, text);
        eligibilityContainer.getChildren().add(row);
    }

    private void checkFlag(Evaluation eval) {
        if (eval.status() == Status.WEAK || eval.status() == Status.CRITICAL) {
            addFlag(eval.label() + " Risk", eval.narrative() + " (" + eval.formattedValue() + ")");
        }
    }

    private void addFlag(String title, String desc) {
        HBox row = new HBox(10);
        row.setStyle("-fx-padding:8; -fx-background-color:#fdedec; -fx-border-color:#fadbd8; -fx-border-radius:4;");
        Label icon = new Label("!");
        icon.setStyle("-fx-text-fill:#c0392b; -fx-font-weight:bold; -fx-font-size:14;");
        
        VBox content = new VBox(2);
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-weight:bold; -fx-text-fill:#c0392b; -fx-font-size:11;");
        Label lblDesc = new Label(desc);
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-text-fill:#2c3e50; -fx-font-size:11;");
        
        content.getChildren().addAll(lblTitle, lblDesc);
        HBox.setHgrow(content, Priority.ALWAYS);
        row.getChildren().addAll(icon, content);
        flagsContainer.getChildren().add(row);
    }

    // --- Decisions ---
    @FXML void submitApprove() { updateStatus("APPROVED"); }
    @FXML void submitCondition() { updateStatus("APPROVED_CONDITIONS"); }
    @FXML void submitDecline() { updateStatus("DECLINED"); }

    private void updateStatus(String status) {
        if (txtComments.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Please enter final comments.").showAndWait();
            return;
        }
        try {
            appRepo.updateStatus(currentAppId, status);
            new Alert(Alert.AlertType.INFORMATION, "Status updated: " + status).showAndWait();
            goDashboard();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goBack() { loadScene("/fxml/applicant_detail.fxml"); }
    private void goDashboard() { loadScene("/fxml/dashboard.fxml"); }

    private void loadScene(String fxml) {
        try {
            Stage stage = (Stage) lblBorrowerName.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            stage.setScene(new Scene(root, 1200, 720));
        } catch (IOException e) { e.printStackTrace(); }
    }
}