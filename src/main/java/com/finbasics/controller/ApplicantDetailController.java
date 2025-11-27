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
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.io.IOException;

public class ApplicantDetailController {

    // Header
    @FXML private Label lblApplicantName, lblApplicantId, lblStatusBadge;
    @FXML private Label lblApplicantType, lblApplicantProduct, lblApplicantAmount;
    @FXML private Label lblAnalysisDate;

    // Metric Grid (M1 - M6)
    @FXML private Label lblM1Title, lblM1Value;
    @FXML private Label lblM2Title, lblM2Value;
    @FXML private Label lblM3Title, lblM3Value;
    @FXML private Label lblM4Title, lblM4Value;
    @FXML private Label lblM5Title, lblM5Value;
    @FXML private Label lblM6Title, lblM6Value;

    @FXML private TextArea txtNotes;

    private final ApplicationRepository appRepo = new ApplicationRepository();
    private final StatementAnalysisRepository saRepo = new StatementAnalysisRepository();

    @FXML
    public void initialize() {
        Integer appId = ApplicationContext.getCurrentApplicationId();
        if (appId == null) return;

        try {
            ApplicationSummary header = appRepo.findHeader(appId);
            StatementAnalysis sa = saRepo.findByApplicationId(appId);
            
            if (header != null) renderHeader(header);
            
            if (sa != null) {
                lblAnalysisDate.setText("Analyzed on: " + (sa.getCreatedAt() != null ? sa.getCreatedAt().substring(0, 10) : "N/A"));
                if ("SME".equalsIgnoreCase(sa.getBorrowerType())) {
                    renderSmeAnalysis(sa);
                } else {
                    renderConsumerAnalysis(sa);
                }
            } else {
                txtNotes.setText("⚠️ Data Error: No statement analysis found for this application ID. Please try re-submitting.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            txtNotes.setText("Error loading data: " + e.getMessage());
        }
    }

    private void renderHeader(ApplicationSummary h) {
        lblApplicantName.setText(h.getBorrowerName());
        lblApplicantId.setText(h.getApplicationNumber());
        lblApplicantType.setText(h.getBorrowerType());
        lblApplicantProduct.setText(h.getProductType());
        lblApplicantAmount.setText(String.format("$%,.0f", h.getRequestedAmount()));
        
        // Status Badge Logic
        String status = h.getStatus();
        lblStatusBadge.setText(status);
        String color = "#95a5a6"; // Grey default
        if (status.contains("ANALYZED") || status.contains("APPROVED")) color = "#27ae60"; // Green
        else if (status.contains("DECLINE")) color = "#c0392b"; // Red
        
        lblStatusBadge.setStyle("-fx-padding:4 12; -fx-background-radius:12; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-color:" + color + ";");
    }

    private void renderSmeAnalysis(StatementAnalysis sa) {
        // 1. Liquidity
        bindMetric(lblM1Title, lblM1Value, "Current Ratio", sa.getCurrentRatio(), "x");
        bindMetric(lblM2Title, lblM2Value, "Quick Ratio", sa.getQuickRatio(), "x");

        // 2. Leverage
        bindMetric(lblM3Title, lblM3Value, "Debt / Equity", sa.getDebtToEquity(), "x");
        bindMetric(lblM4Title, lblM4Value, "DSCR (Cash Flow)", sa.getDscr(), "x");

        // 3. Profitability
        bindMetric(lblM5Title, lblM5Value, "Net Margin", sa.getNetMargin() * 100, "%");
        bindMetric(lblM6Title, lblM6Value, "ROE", sa.getRoe() * 100, "%");

        // Auto-generate narrative
        StringBuilder sb = new StringBuilder();
        sb.append("SME CREDIT ASSESSMENT:\n\n");
        sb.append(sa.getDscr() >= 1.25 
            ? "✅ Strong Debt Service Coverage. The business generates sufficient cash flow (>1.25x) to cover the new loan." 
            : "⚠️ Weak Debt Service Coverage. DSCR is below 1.25x; the business may struggle to make payments.");
        sb.append("\n\n");
        sb.append(sa.getCurrentRatio() >= 1.0 
            ? "✅ Positive Liquidity. Current assets cover short-term obligations." 
            : "⚠️ Liquidity Tightness. Current Ratio below 1.0 indicates potential working capital stress.");
        txtNotes.setText(sb.toString());
    }

    private void renderConsumerAnalysis(StatementAnalysis sa) {
        // 1. Affordability
        bindMetric(lblM1Title, lblM1Value, "DTI Ratio", sa.getDti() * 100, "%");
        bindMetric(lblM2Title, lblM2Value, "Monthly Income", sa.getMonthlyIncome(), "$");

        // 2. Collateral / Risk
        bindMetric(lblM3Title, lblM3Value, "LTV (Collateral)", sa.getLtv() * 100, "%");
        bindMetric(lblM4Title, lblM4Value, "Credit Score", (double)sa.getCreditScore(), "");

        // 3. Balance Sheet
        bindMetric(lblM5Title, lblM5Value, "Total Assets", sa.getTotalAssets(), "$");
        bindMetric(lblM6Title, lblM6Value, "Net Worth", sa.getTotalAssets() - sa.getTotalLiabilities(), "$");

        // Auto-generate narrative
        StringBuilder sb = new StringBuilder();
        sb.append("CONSUMER CREDIT ASSESSMENT:\n\n");
        sb.append(sa.getDti() <= 0.43 
            ? "✅ Affordability Confirmed. DTI is within standard limits (<= 43%)." 
            : "⚠️ High DTI Warning. Ratios above 43% suggest the borrower is over-leveraged.");
        sb.append("\n\n");
        sb.append(sa.getCreditScore() >= 700 
            ? "✅ Strong Credit Profile (700+). Indicates reliable repayment history." 
            : "ℹ Sub-Prime Credit Score. Consider requiring a co-signer or higher down payment.");
        txtNotes.setText(sb.toString());
    }

    private void bindMetric(Label titleLbl, Label valueLbl, String title, double value, String suffix) {
        titleLbl.setText(title);
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            valueLbl.setText("N/A");
        } else if ("$".equals(suffix)) {
            valueLbl.setText(String.format("$%,.0f", value));
        } else {
            valueLbl.setText(String.format("%,.2f%s", value, suffix));
        }
    }

    @FXML
    private void goHome() {
        try {
            Stage stage = (Stage) lblApplicantName.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
            stage.setScene(new Scene(root, 1200, 720));
            stage.setTitle("FinBasics Underwriter - Dashboard");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void closeDetail() { goHome(); }
    @FXML private void openRiskAssessment() {}
    @FXML private void openLoanStructuring() {}
}