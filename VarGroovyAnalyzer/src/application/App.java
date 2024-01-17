package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    public static final Image ICON = new Image("img\\groovy_icon.png");
    public static final String ABOUT = "files\\about.txt";

    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("main_window.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("Groovy Analyzer");
        stage.getIcons().add(ICON);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}