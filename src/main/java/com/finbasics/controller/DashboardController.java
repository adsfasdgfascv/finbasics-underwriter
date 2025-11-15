package com.finbasics.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import com.finbasics.service.Session;
import javafx.fxml.FXMLLoader;

/**
 * Controller for the dashboard screen after successful login.
 * 
 * Displays main application features and handles user logout.
 */
public class DashboardController {

    @FXML private Button logout;

    /**
     * Logout action: clear session and return to login screen.
     */
    @FXML
    private void logout() {
        // Clear the current session
        Session.clear();
        
        try {
            // Reload login screen
            Stage stage = (Stage) logout.getScene().getWindow();
            var url = getClass().getResource("/fxml/login.fxml");
            if (url != null) {
                Parent root = FXMLLoader.load(url);
                stage.setScene(new Scene(root, 1200, 720));
                stage.setTitle("FinBasics Underwriter - Login");
            }
        } catch (Exception ex) {
            System.err.println("Failed to return to login: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
