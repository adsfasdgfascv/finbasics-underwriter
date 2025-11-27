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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class ApplicantDetailController {

    @FXML private Label lblApplicantName, lblApplicantId, lblStatusBadge;
    @FXML private Label lblApplicantType, lblApplicantProduct, lblApplicantAmount, lblAnalysisDate;
    @FXML private TextArea txtNotes;

    @FXML private Label lblM1Title, lblM1Value;
    @FXML private Label lblM2Title, lblM2Value;
    @FXML private Label lblM3Title, lblM3Value;
    @FXML private Label lblM4Title, lblM4Value;
    @FXML private Label lblM5Title, lblM5Value;
    @FXML private Label lblM6Title, lblM6Value;

    private final ApplicationRepository appRepo = new ApplicationRepository();
    private final StatementAnalysisRepository saRepo = new StatementAnalysisRepository();

    @FXML
    public void initialize() {
        Integer appId = ApplicationContext.getCurrentApplicationId();
        if (appId == null) return;

        try {
            ApplicationSummary h = appRepo.findHeader(appId);
            StatementAnalysis sa = saRepo.findByApplicationId(appId);

            if (h != null) {
                lblApplicantName.setText(h.getBorrowerName());
                lblApplicantId.setText(h.getApplicationNumber());
                lblApplicantType.setText(h.getBorrowerType());
                lblApplicantProduct.setText(h.getProductType());
                lblApplicantAmount.setText(String.format("$%,.0f", h.getRequestedAmount()));
                lblStatusBadge.setText(h.getStatus());
            }

            if (sa != null) {
                lblAnalysisDate.setText("Analyzed: " + sa.getCreatedAt().substring(0, 10));
                if ("SME".equalsIgnoreCase(sa.getBorrowerType())) renderSme(sa);
                else renderConsumer(sa);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderSme(StatementAnalysis sa) {
        bind(lblM1Title, lblM1Value, FinancialCalculator.evalDscr(sa.getDscr()));
        bind(lblM2Title, lblM2Value, FinancialCalculator.evalCurrentRatio(sa.getCurrentRatio()));
        bind(lblM3Title, lblM3Value, FinancialCalculator.evalDebtEquity(sa.getDebtToEquity()));
        bind(lblM4Title, lblM4Value, FinancialCalculator.evalQuickRatio(sa.getQuickRatio()));
        bind(lblM5Title, lblM5Value, FinancialCalculator.evalNetMargin(sa.getNetMargin()));
        bind(lblM6Title, lblM6Value, FinancialCalculator.evalROE(sa.getRoe()));
        txtNotes.setText("SME Analysis: " + FinancialCalculator.calculateRiskGrade(sa));
    }

    private void renderConsumer(StatementAnalysis sa) {
        bind(lblM1Title, lblM1Value, FinancialCalculator.evalDti(sa.getDti()));
        bind(lblM2Title, lblM2Value, FinancialCalculator.evalCreditScore(sa.getCreditScore()));
        bind(lblM3Title, lblM3Value, FinancialCalculator.evalLtv(sa.getLtv()));
        lblM4Title.setText("Monthly Income"); lblM4Value.setText(String.format("$%,.0f", sa.getMonthlyIncome()));
        lblM5Title.setText("Total Assets"); lblM5Value.setText(String.format("$%,.0f", sa.getTotalAssets()));
        lblM6Title.setText("Net Worth"); lblM6Value.setText(String.format("$%,.0f", sa.getTotalAssets() - sa.getTotalLiabilities()));
        txtNotes.setText("Consumer Analysis: " + FinancialCalculator.calculateRiskGrade(sa));
    }

    private void bind(Label t, Label v, Evaluation e) {
        t.setText(e.label());
        v.setText(e.formatted());
    }

    @FXML private void goHome() { loadScene("/fxml/dashboard.fxml"); }
    @FXML private void openRiskAssessment() { loadScene("/fxml/risk_assessment.fxml"); }
    @FXML private void openLoanStructuring() {} // TODO

    private void loadScene(String fxml) {
        try {
            Stage stage = (Stage) lblApplicantName.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            stage.setScene(new Scene(root, 1200, 720));
        } catch (IOException e) { e.printStackTrace(); }
    }
}