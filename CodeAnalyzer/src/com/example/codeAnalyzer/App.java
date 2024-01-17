package com.example.codeAnalyzer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    public static final Image icon = new Image("img\\ruby_icon.png");

    public static void createWindow(String file, String title) throws IOException {
        createWindow(file, title, new Stage());
    }
    public static void createWindow(String file, String title, Stage stage) throws IOException {
        Parent root = FXMLLoader.load(App.class.getResource(file));
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.getIcons().add(icon);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        createWindow("mainWindow.fxml", "Ruby code analyzer", stage);
    }
}