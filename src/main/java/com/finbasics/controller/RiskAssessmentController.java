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

    private void generateRiskAssessment(StatementAnalysis sa) {
        factorsContainer.getChildren().clear();
        String grade = FinancialCalculator.calculateRiskGrade(sa);
        lblRiskGrade.setText(grade);

        if (grade.contains("Tier 1")) lblRecommendation.setText("Recommended for Approval.");
        else if (grade.contains("Tier 2")) lblRecommendation.setText("Approval with Conditions.");
        else lblRecommendation.setText("Decline Recommended.");
        
        if ("SME".equalsIgnoreCase(sa.getBorrowerType())) {
            addFactor(FinancialCalculator.evalDscr(sa.getDscr()));
            addFactor(FinancialCalculator.evalCurrentRatio(sa.getCurrentRatio()));
            addFactor(FinancialCalculator.evalDebtEquity(sa.getDebtToEquity()));
        } else {
            addFactor(FinancialCalculator.evalDti(sa.getDti()));
            addFactor(FinancialCalculator.evalCreditScore(sa.getCreditScore()));
            if (sa.getLtv() > 0.01) addFactor(FinancialCalculator.evalLtv(sa.getLtv()));
        }
    }

    private void addFactor(Evaluation eval) {
        HBox row = new HBox(15);
        row.setStyle("-fx-padding:10; -fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:4;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        titleBox.setPrefWidth(120);
        Label title = new Label(eval.label());
        title.setStyle("-fx-font-weight:bold; -fx-text-fill:#2c3e50;");
        Label value = new Label(eval.formatted());
        value.setStyle("-fx-font-size:14px;");
        titleBox.getChildren().addAll(title, value);

        Label statusLbl = new Label(eval.status().toString());
        String color = switch (eval.status()) {
            case STRONG -> "#27ae60";
            case ACCEPTABLE -> "#f39c12";
            default -> "#c0392b";
        };
        statusLbl.setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold; -fx-min-width:80;");

        Label note = new Label(eval.note());
        note.setWrapText(true);
        HBox.setHgrow(note, Priority.ALWAYS);

        row.getChildren().addAll(titleBox, statusLbl, note);
        factorsContainer.getChildren().add(row);
    }

    @FXML void submitApprove() { updateStatus("APPROVED"); }
    @FXML void submitCondition() { updateStatus("APPROVED_CONDITIONS"); }
    @FXML void submitDecline() { updateStatus("DECLINED"); }

    private void updateStatus(String status) {
        try {
            appRepo.updateStatus(currentAppId, status);
            new Alert(Alert.AlertType.INFORMATION, "Status updated to: " + status).showAndWait();
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