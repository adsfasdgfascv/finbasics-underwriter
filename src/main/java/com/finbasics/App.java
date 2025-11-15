package com.finbasics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import com.finbasics.persistence.Database;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Initialize the database: creates tables, seeds data if needed.
            Database.init();
        } catch (Exception e) {
            System.err.println("FATAL: Database initialization failed");
            e.printStackTrace();
            throw new RuntimeException("Cannot start app without database", e);
        }

        // Locate the login FXML resource
        URL url = getClass().getResource("/fxml/login.fxml");
        if (url == null) {
            throw new IllegalStateException("Missing /fxml/login.fxml - ensure it exists under src/main/resources/fxml/");
        }

        try {
            // Load the FXML and show the primary stage.
            Parent root = FXMLLoader.load(url);
            stage.setTitle("FinBasics Underwriter");
            stage.setScene(new Scene(root, 1200, 720));
            stage.show();
        } catch (IOException e) {
            System.err.println("FATAL: Failed to load login.fxml");
            e.printStackTrace();
            throw new RuntimeException("Cannot load login screen", e);
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}


