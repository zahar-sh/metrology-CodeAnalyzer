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

    @FXML
    private void initialize() {
        clear();
        analyzeFile(new File("code.txt"));
    }

    @FXML
    private void analyze() {
        tokenizer.setInput(code.getText());
        out.setText(analyzer.analyze(tokenizer));
    }

    @FXML
    private void openFile() {
        File input = fileChooser.showOpenDialog(null);
        if (input != null)
            analyzeFile(input);
    }

    @FXML
    private void clear() {
        code.setText("");
        out.setText("Enter code");
    }

    private void analyzeFile(File input) {
        try {
            tokenizer.setInput(input);
            out.setText(analyzer.analyze(tokenizer));
            code.setText(tokenizer.toString());
        } catch (IOException e) {
            out.setText(e.getMessage());
        }
    }
}