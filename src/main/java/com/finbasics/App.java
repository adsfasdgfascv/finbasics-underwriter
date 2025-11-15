package com.finbasics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlUrl = getClass().getResource("/fxml/login.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML not found: /fxml/login.fxml - ensure it exists under src/main/resources/fxml/");
        }

        Parent root = FXMLLoader.load(fxmlUrl);
        stage.setTitle("FinBasics Underwriter");
        stage.setScene(new Scene(root, 1200, 720));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


