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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;

public class ApplicantDetailController {

    @FXML private Label lblApplicantHeader, lblApplicantName, lblApplicantType, lblApplicantProduct;
    @FXML private Label lblApplicantAmount, lblApplicantStatus, lblKeyNotes1, lblKeyNotes2;
    @FXML private HBox boxSmeLegend, boxConsumerLegend;
    @FXML private Label lblCircle1, lblCircle2, lblCircle3, lblCircleMiddle, lblCircle4, lblCircle5;

    private final ApplicationRepository appRepo = new ApplicationRepository();
    private final StatementAnalysisRepository saRepo = new StatementAnalysisRepository();

    @FXML
    public void initialize() {
        Integer appId = ApplicationContext.getCurrentApplicationId();
        if (appId == null) return;

        try {
            ApplicationSummary header = appRepo.findHeader(appId);
            StatementAnalysis sa = saRepo.findByApplicationId(appId);
            if (header == null || sa == null) return;

            populateHeader(header, sa);
            if ("SME".equalsIgnoreCase(sa.getBorrowerType())) {
                renderSme(sa);
            } else {
                renderConsumer(sa);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateHeader(ApplicationSummary h, StatementAnalysis sa) {
        lblApplicantHeader.setText("Applicant " + h.getApplicationNumber());
        lblApplicantName.setText(h.getBorrowerName());
        lblApplicantType.setText("Type: " + h.getBorrowerType());
        lblApplicantProduct.setText("Product: " + h.getProductType());
        lblApplicantAmount.setText(String.format("Requested: $%,.0f", h.getRequestedAmount()));
        lblApplicantStatus.setText("Status: " + h.getStatus());

        if ("SME".equalsIgnoreCase(sa.getBorrowerType())) {
            lblKeyNotes1.setText(String.format("DSCR %.2fx; Net margin %.1f%%; ROE %.1f%%",
                    sa.getDscr(), sa.getNetMargin() * 100, sa.getRoe() * 100));
            lblKeyNotes2.setText(String.format("Liquidity: CR %.2f×, QR %.2f×; D/E %.2f×",
                    sa.getCurrentRatio(), sa.getQuickRatio(), sa.getDebtToEquity()));
        } else {
            lblKeyNotes1.setText(String.format("DTI %.1f%%; LTV %s",
                    sa.getDti() * 100, sa.getLtv() == 0 ? "N/A" : String.format("%.0f%%", sa.getLtv() * 100)));
            lblKeyNotes2.setText(String.format("Monthly income $%,.0f; total debt $%,.0f; score ~ %d",
                    sa.getMonthlyIncome(), sa.getMonthlyDebtPayments(), sa.getCreditScore()));
        }
    }

    private void renderSme(StatementAnalysis sa) {
        boxSmeLegend.setVisible(true);
        boxSmeLegend.setManaged(true); // Ensure layout reserves space
        
        boxConsumerLegend.setVisible(false);
        boxConsumerLegend.setManaged(false); // Collapse space

        lblCircle1.setText(String.format("Liquidity\nCR %.2f×\nQR %.2f×", sa.getCurrentRatio(), sa.getQuickRatio()));
        lblCircle2.setText(String.format("Efficiency\nDSO %.0f days\nInv. Turn %.2f×", sa.getDso(), sa.getInventoryTurnover()));
        lblCircle3.setText(String.format("Leverage\nD/E %.2f×\nDSCR %.2f×", sa.getDebtToEquity(), sa.getDscr()));
        lblCircleMiddle.setText(String.format("Profitability\nEBITDA %.1f%%\nNet %.1f%%", sa.getEbitdaMargin() * 100, sa.getNetMargin() * 100));
        lblCircle4.setText(String.format("Returns\nROA %.1f%%\nROE %.1f%%", sa.getRoa() * 100, sa.getRoe() * 100));

        double equity = sa.getTotalAssets() - sa.getTotalLiabilities();
        if (equity <= 0) equity = 1;
        lblCircle5.setText(String.format("DuPont\nROE ≈ %.1f%%\n= Margin × ATO × EM\n= %.1f%% × %.2f × %.2f",
                sa.getRoe() * 100, sa.getNetMargin() * 100, sa.getAssetTurnover(), sa.getTotalAssets() / equity));
    }

    private void renderConsumer(StatementAnalysis sa) {
        boxSmeLegend.setVisible(false);
        boxSmeLegend.setManaged(false); // Collapse space
        
        boxConsumerLegend.setVisible(true);
        boxConsumerLegend.setManaged(true); // Ensure layout reserves space

        lblCircle1.setText(String.format("Affordability\nDTI %.1f%%", sa.getDti() * 100));
        lblCircle2.setText(sa.getLtv() == 0.0 ? "Collateral\nUnsecured\nLTV N/A" : String.format("Collateral\nLTV %.0f%%", sa.getLtv() * 100));
        lblCircle3.setText(String.format("Credit Profile\nScore ~ %d", sa.getCreditScore()));
        lblCircleMiddle.setText(String.format("Cash Flow\nFree cash / pay ≈ %.2f×", sa.getDscr()));
        lblCircle4.setText(String.format("Income\nMonthly $%,.0f\nDebt $%,.0f", sa.getMonthlyIncome(), sa.getMonthlyDebtPayments()));
        lblCircle5.setText(String.format("Balance\nAssets $%,.0f\nLiab. $%,.0f", sa.getTotalAssets(), sa.getTotalLiabilities()));
    }

    @FXML private void goHome() {
        try {
            Stage stage = (Stage) lblApplicantHeader.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
            stage.setScene(new Scene(root, 1200, 720));
            stage.setTitle("FinBasics Underwriter - Dashboard");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void closeDetail() { goHome(); }
    @FXML private void openRiskAssessment() {}
    @FXML private void openLoanStructuring() {}
}