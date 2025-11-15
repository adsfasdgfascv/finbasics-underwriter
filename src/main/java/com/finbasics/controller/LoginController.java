package com.finbasics.controller;


import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.finbasics.model.User;
import com.finbasics.service.AuthService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Handles all user interactions on the login screen.
 * 
 * Controls login modal visibility, blur effects, and credential validation.
 * Connects to login.fxml for UI elements and event bindings.
 */
public class LoginController {

    @FXML private Group backgroundLayer;
    @FXML private StackPane modalLayer;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    
    private final GaussianBlur blur = new GaussianBlur(24);
    private final AuthService auth = new AuthService();

    /**
     * Opens login modal and applies blur effect to background.
     */
    @FXML
    private void openLogin() {
        backgroundLayer.setEffect(blur);
        modalLayer.setVisible(true);
    }

    /**
     * Closes login modal and removes blur effect from background.
     * Clears input fields for next login attempt.
     */
    @FXML
    private void closeLogin() {
        modalLayer.setVisible(false);
        backgroundLayer.setEffect(null);
        // Clear fields to prevent old input from showing
        usernameField.clear();
        passwordField.clear();
    }

    /**
     * Validates login credentials and authenticates user.
     * 
     * TODO: Implement real validation against database using PasswordHasher.
     * Currently in demo mode - just shows success alert.
     */
    @FXML
    private void signIn() {
        try {
            if (usernameField.getText().isBlank() || passwordField.getText().isBlank()) {
                throw new IllegalArgumentException("Please enter username and password.");
            }
            User u = auth.login(usernameField.getText().trim(), passwordField.getText());
            Stage stage = (Stage) modalLayer.getScene().getWindow();
            
            // Load dashboard FXML
            var dashboardUrl = getClass().getResource("/fxml/dashboard.fxml");
            if (dashboardUrl == null) {
                throw new IllegalStateException("Missing /fxml/dashboard.fxml resource");
            }
            Parent root = FXMLLoader.load(dashboardUrl);
            stage.setScene(new Scene(root, 1200, 720));
            stage.setTitle("Dashboard - " + u.getusername());
        } catch (IllegalStateException ex) {
            // Resource not found
            new Alert(Alert.AlertType.ERROR, "Application error: " + ex.getMessage()).showAndWait();
        } catch (Exception ex) {
            // DB auth errors or other exceptions
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }


    /**
     * Handles "Forgot Password" action.
     * 
     * TODO: Implement password recovery flow.
     * Currently shows demo placeholder.
     */
    @FXML
    private void onForgot() {
        new Alert(Alert.AlertType.INFORMATION, "Password reset flow (demo)").showAndWait();
    }
}
