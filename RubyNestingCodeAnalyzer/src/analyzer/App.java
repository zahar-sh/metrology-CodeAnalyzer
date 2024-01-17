package analyzer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("main_window.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("Analyzer");
        stage.getIcons().add(Util.ICON);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}