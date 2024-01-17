package analyzer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainWindow {
    @FXML
    private Label out;

    @FXML
    private TextArea code;

    private final Analyzer analyzer = new Analyzer();
    private final Tokenizer tokenizer = new Tokenizer();
    private final FileChooser fileChooser = new FileChooser();
    private Stage stage = null;

    @FXML
    private void initialize() {
        clear();
    }

    @FXML
    private void analyze() {
        tokenizer.setInput(code.getText());
        out.setText(analyzer.analyze(tokenizer));
    }

    @FXML
    private void openFile() {
        File input = fileChooser.showOpenDialog(null);
        if (input != null) {
            try {
                tokenizer.setInput(input);
                out.setText(analyzer.analyze(tokenizer));
                code.setText(tokenizer.toString());
            } catch (IOException e) {
                out.setText(e.getMessage());
            }
        }
    }

    @FXML
    private void clear() {
        code.setText("");
        out.setText("Enter code");
    }

    @FXML
    private void createAboutWindow() {
        if (stage == null) {
            try {
                Stage s = new Stage();
                s.setScene(new Scene(FXMLLoader.load(getClass().
                        getResource("about_window.fxml"))));
                s.setTitle("About");
                s.setResizable(false);
                s.getIcons().add(Util.ICON);
                s.setOnCloseRequest(windowEvent -> stage = null);
                s.show();
                stage = s;
            } catch (IOException ignored) {
            }
        }
    }
}