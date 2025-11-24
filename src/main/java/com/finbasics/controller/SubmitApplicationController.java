package com.finbasics.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class SubmitApplicationController {
    @FXML private VBox viewStep1, viewStep2, viewStep3;
    @FXML private HBox Step1, Step2, Step3;
    @FXML private Button backBtn, nextBtn;

    private int currentStep = 1;

    @FXML
    private void goNext() {
        if(currentStep < 3){
            currentStep++;
            updateView();
        }else{
            submitAndFinish();
        }
    }

    @FXML
    private void goBack() {
        if(currentStep >1){
            currentStep--;
            updateView();
        }
    }

    @FXML
    private void updateView() {
        viewStep1.setVisible(currentStep == 1);
        viewStep2.setVisible(currentStep == 2);
        viewStep3.setVisible(currentStep == 3);

        backBtn.setDisable(currentStep ==1);
        nextBtn.setText(currentStep == 3 ? "Create Application" : "Next Step →");

        highlightStep(Step1, currentStep >= 1);
        highlightStep(Step2, currentStep >= 2);
        highlightStep(Step3, currentStep >= 3);
    }

    @FXML
    private void highlightStep(HBox box, boolean active) {
        if(active){
            box.setStyle("-fx-padding: 15 20; -fx-background-color: #e8f6f3; -fx-border-color: #27ae60; -fx-border-width: 0 0 0 4;");
        }else{
            box.setStyle("-fx-padding: 15 20; -fx-background-color: transparent;");
        }
    }

    @FXML
    private void cancel() {
        returnToDashboard();
    }

    @FXML
    private void submitAndFinish() {
        
    }

    @FXML
    private void returnToDashboard() {
        try{
            Stage stage = (Stage) nextBtn.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
            stage.setScene(new Scene(root, 1200,720));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}
