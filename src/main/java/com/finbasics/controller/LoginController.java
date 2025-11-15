package com.finbasics.controller;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.control.*;

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
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // Validate both fields are filled
        if (username.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Please enter your username.").showAndWait();
            return;
        }
        
        if (password.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Please enter your password.").showAndWait();
            return;
        }
        
        // TODO: Replace demo code with real database validation:
        // try {
        //     User user = Database.validateLogin(username, password);
        //     if (user != null) {
        //         new Alert(Alert.AlertType.INFORMATION, "Welcome, " + user.getUsername()).showAndWait();
        //         closeLogin();
        //         // TODO: Navigate to dashboard
        //     } else {
        //         new Alert(Alert.AlertType.ERROR, "Invalid username or password.").showAndWait();
        //     }
        // } catch (Exception e) {
        //     new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage()).showAndWait();
        // }
        
        // Demo mode - remove when real auth is implemented
        new Alert(Alert.AlertType.INFORMATION, "Signed in! (demo)").showAndWait();
        closeLogin();
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
