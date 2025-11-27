package com.finbasics.controller;

import com.finbasics.model.ApplicationSummary;
import com.finbasics.model.StatementAnalysis;
import com.finbasics.persistence.ApplicationRepository;
import com.finbasics.persistence.StatementAnalysisRepository;
import com.finbasics.service.ApplicationContext;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class ApplicantDetailController {

    @FXML private Label lblApplicantName, lblApplicantId, lblStatusBadge;
    @FXML private Label lblApplicantType, lblApplicantProduct, lblApplicantAmount, lblAnalysisDate;

    // Metric Grid slots (T=Title, V=Value)
    @FXML private Label lblM1T, lblM1V, lblM2T, lblM2V, lblM3T, lblM3V, lblM4T, lblM4V;
    @FXML private Label lblM5T, lblM5V, lblM6T, lblM6V, lblM7T, lblM7V, lblM8T, lblM8V;
    @FXML private Label lblM9T, lblM9V, lblM10T, lblM10V, lblM11T, lblM11V, lblM12T, lblM12V;

    private final ApplicationRepository appRepo = new ApplicationRepository();
    private final StatementAnalysisRepository saRepo = new StatementAnalysisRepository();
    private Integer currentAppId;

    @FXML
    public void initialize() {
        currentAppId = ApplicationContext.getCurrentApplicationId();
        if (currentAppId == null) return;

        try {
            ApplicationSummary h = appRepo.findHeader(currentAppId);
            StatementAnalysis sa = saRepo.findByApplicationId(currentAppId);

            if (h != null) {
                lblApplicantName.setText(h.getBorrowerName());
                lblApplicantId.setText(h.getApplicationNumber());
                lblApplicantType.setText(h.getBorrowerType());
                lblApplicantProduct.setText(h.getProductType());
                lblApplicantAmount.setText(String.format("$%,.0f", h.getRequestedAmount()));
                updateStatusBadge(h.getStatus());
            }

            if (sa != null) {
                lblAnalysisDate.setText("Analyzed: " + sa.getCreatedAt().substring(0, 10));
                if ("SME".equalsIgnoreCase(sa.getBorrowerType())) renderSme(sa);
                else renderConsumer(sa);
            } else {
                 lblAnalysisDate.setText("No analysis data available.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateStatusBadge(String status) {
        lblStatusBadge.setText(status == null ? "PENDING" : status);
        String color = switch (status == null ? "" : status) {
            case "APPROVED", "APPROVED_CONDITIONS" -> "#27ae60"; // Green
            case "DECLINED" -> "#c0392b"; // Red
            case "ANALYZED" -> "#2980b9"; // Blue
            default -> "#95a5a6"; // Grey
        };
        lblStatusBadge.setStyle("-fx-padding:4 12; -fx-background-radius:12; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-color:" + color + ";");
    }

    private void renderSme(StatementAnalysis sa) {
        // Row 1: Key Ratios
        bind(lblM1T, lblM1V, "DSCR", sa.getDscr(), "x");
        bind(lblM2T, lblM2V, "Current Ratio", sa.getCurrentRatio(), "x");
        bind(lblM3T, lblM3V, "Quick Ratio", sa.getQuickRatio(), "x");
        bind(lblM4T, lblM4V, "Debt / Equity", sa.getDebtToEquity(), "x");

        // Row 2: Profitability & Efficiency
        bind(lblM5T, lblM5V, "Net Margin", sa.getNetMargin() * 100, "%");
        bind(lblM6T, lblM6V, "ROE", sa.getRoe() * 100, "%");
        bind(lblM7T, lblM7V, "ROA", sa.getRoa() * 100, "%");
        bind(lblM8T, lblM8V, "Asset Turnover", sa.getAssetTurnover(), "x");

        // Row 3: Balance Sheet High-Level
        bind(lblM9T, lblM9V, "Total Assets", sa.getTotalAssets(), "$");
        bind(lblM10T, lblM10V, "Total Liabilities", sa.getTotalLiabilities(), "$");
        bind(lblM11T, lblM11V, "Net Worth", sa.getTotalAssets() - sa.getTotalLiabilities(), "$");
        clear(lblM12T, lblM12V); // Empty slot
    }

    private void renderConsumer(StatementAnalysis sa) {
        // Row 1: Key Risk Ratios
        bind(lblM1T, lblM1V, "DTI Ratio", sa.getDti() * 100, "%");
        bind(lblM2T, lblM2V, "Credit Score", (double)sa.getCreditScore(), "");
        bind(lblM3T, lblM3V, "LTV (Collateral)", sa.getLtv() * 100, "%");
        clear(lblM4T, lblM4V); // Empty slot

        // Row 2: Income & Debt
        bind(lblM5T, lblM5V, "Monthly Income", sa.getMonthlyIncome(), "$");
        bind(lblM6T, lblM6V, "Monthly Debt", sa.getMonthlyDebtPayments(), "$");
        bind(lblM7T, lblM7V, "Free Cash Flow", sa.getMonthlyIncome() - sa.getMonthlyDebtPayments(), "$");
        clear(lblM8T, lblM8V);

        // Row 3: Balance Sheet
        bind(lblM9T, lblM9V, "Total Assets", sa.getTotalAssets(), "$");
        bind(lblM10T, lblM10V, "Total Liabilities", sa.getTotalLiabilities(), "$");
        bind(lblM11T, lblM11V, "Net Worth", sa.getTotalAssets() - sa.getTotalLiabilities(), "$");
        clear(lblM12T, lblM12V);
    }

    private void bind(Label t, Label v, String title, double val, String suffix) {
        t.setText(title);
        if (Double.isNaN(val) || Double.isInfinite(val)) v.setText("N/A");
        else if ("$".equals(suffix)) v.setText(String.format("$%,.0f", val));
        else v.setText(String.format("%,.2f%s", val, suffix));
    }

    private void clear(Label t, Label v) { t.setText(""); v.setText(""); }

    @FXML private void goHome() { loadScene("/fxml/dashboard.fxml"); }

    @FXML private void openRiskAssessment() {
        if (lblAnalysisDate.getText().contains("No analysis")) {
             new Alert(Alert.AlertType.WARNING, "Cannot proceed. No financial analysis data found for this applicant.").showAndWait();
             return;
        }
        loadScene("/fxml/risk_assessment.fxml");
    }

    private void loadScene(String fxml) {
        try {
            Stage stage = (Stage) lblApplicantName.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            stage.setScene(new Scene(root, 1200, 720));
        } catch (IOException e) { e.printStackTrace(); }
    }
}