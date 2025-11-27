package com.finbasics.controller;

import com.finbasics.model.ApplicationSummary;
import com.finbasics.model.StatementAnalysis;
import com.finbasics.persistence.ApplicationRepository;
import com.finbasics.persistence.StatementAnalysisRepository;
import com.finbasics.service.ApplicationContext;
import com.finbasics.service.FinancialCalculator;
import com.finbasics.service.FinancialCalculator.Evaluation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class RiskAssessmentController {

    @FXML private Label lblBorrowerName, lblAppNumber, lblProduct, lblAmount;
    @FXML private HBox tierBanner;
    @FXML private Label lblRiskGrade, lblRecommendation;
    @FXML private VBox factorsContainer;
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
                generateRiskAssessment(analysis);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void populateHeader(ApplicationSummary s) {
        lblBorrowerName.setText(s.getBorrowerName());
        lblAppNumber.setText(s.getApplicationNumber());
        lblProduct.setText(s.getProductType());
        lblAmount.setText(String.format("$%,.0f", s.getRequestedAmount()));
    }

    private void generateRiskAssessment(StatementAnalysis sa) {
        factorsContainer.getChildren().clear();
        String tier = FinancialCalculator.calculateOverallRiskGrade(sa);
        lblRiskGrade.setText(tier);

        // Style the banner based on the tier
        String bannerStyle = "-fx-padding:20; -fx-background-radius:8; -fx-background-color:";
        String recText;
        if (tier.contains("Tier 1")) {
            bannerStyle += "#d5f5e3;"; // Light Green
            recText = "Strong profile. Recommended for standard approval.";
        } else if (tier.contains("Tier 2")) {
            bannerStyle += "#fcf3cf;"; // Light Yellow
            recText = "Moderate risk. Approval recommended subject to standard conditions.";
        } else if (tier.contains("Tier 3")) {
            bannerStyle += "#fadbd8;"; // Light Red
            recText = "High risk. Consider decline or approval only with strong mitigants and tight conditions.";
        } else {
            bannerStyle += "#ebedef;"; // Grey
            recText = "Critical risk. Decline recommended.";
        }
        tierBanner.setStyle(bannerStyle);
        lblRecommendation.setText(recText);
        
        // Dynamically add risk factor rows
        if ("SME".equalsIgnoreCase(sa.getBorrowerType())) {
            addFactorRow(FinancialCalculator.evaluateSmeDscr(sa.getDscr()));
            addFactorRow(FinancialCalculator.evaluateSmeLeverage(sa.getDebtToEquity()));
            addFactorRow(FinancialCalculator.evaluateSmeCurrentRatio(sa.getCurrentRatio()));
            addFactorRow(FinancialCalculator.evaluateSmeProfitability(sa.getNetMargin()));
        } else {
            addFactorRow(FinancialCalculator.evaluateConsumerDti(sa.getDti()));
            addFactorRow(FinancialCalculator.evaluateConsumerCreditScore(sa.getCreditScore()));
            if (sa.getLtv() > 0.01) addFactorRow(FinancialCalculator.evaluateConsumerLtv(sa.getLtv()));
        }
    }

    private void addFactorRow(Evaluation eval) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10 15; -fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 0 4;");

        Label titleStr = new Label(eval.label());
        titleStr.setStyle("-fx-font-weight:bold; -fx-text-fill:#7f8c8d; -fx-min-width:100;");
        
        Label valueStr = new Label(eval.formattedValue());
        valueStr.setStyle("-fx-font-weight:bold; -fx-font-size:13; -fx-min-width:80;");

        Label statusBadge = new Label(eval.status().toString());
        String statusStyle = switch (eval.status()) {
            case STRONG -> "-fx-background-color:#d5f5e3; -fx-text-fill:#27ae60;";
            case ACCEPTABLE -> "-fx-background-color:#fcf3cf; -fx-text-fill:#f39c12;";
            default -> "-fx-background-color:#fadbd8; -fx-text-fill:#c0392b;";
        };
        statusBadge.setStyle(statusStyle + " -fx-padding:2 8; -fx-background-radius:10; -fx-font-weight:bold; -fx-font-size:10;");
        
        Label narrative = new Label(eval.narrative());
        narrative.setWrapText(true);
        HBox.setHgrow(narrative, Priority.ALWAYS);

        row.setStyle(row.getStyle() + (eval.status() == FinancialCalculator.Status.STRONG ? "-fx-border-color:#27ae60;" : eval.status() == FinancialCalculator.Status.ACCEPTABLE ? "-fx-border-color:#f39c12;" : "-fx-border-color:#c0392b;"));

        row.getChildren().addAll(titleStr, valueStr, statusBadge, narrative);
        factorsContainer.getChildren().add(row);
    }

    @FXML void submitApprove() { updateStatusAndFinish("APPROVED"); }
    @FXML void submitCondition() { updateStatusAndFinish("APPROVED_CONDITIONS"); }
    @FXML void submitDecline() { updateStatusAndFinish("DECLINED"); }

    private void updateStatusAndFinish(String newStatus) {
        if (txtComments.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Please enter comments before submitting the decision.").showAndWait();
            return;
        }
        try {
            appRepo.updateStatus(currentAppId, newStatus);
            new Alert(Alert.AlertType.INFORMATION, "Decision Submitted. Application status updated to: " + newStatus).showAndWait();
            goDashboard();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to update status: " + e.getMessage()).showAndWait();
        }
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