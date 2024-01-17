package com.example.codeAnalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class InfoWindowController {
    @FXML
    private ImageView imageView;

    @FXML
    private Label textLabel;

    @FXML
    void initialize() throws IOException {
        imageView.setImage(App.icon);
        textLabel.setText(getText());
    }

    private static String text;
    private static String getText() throws IOException {
        if (text == null) {
            try (InputStreamReader reader = new InputStreamReader(
                    Objects.requireNonNull(InfoWindowController.class.getClassLoader()
                            .getResourceAsStream("file\\info.txt")))) {
                StringBuilder stringBuilder = new StringBuilder();
                int c;
                while ((c = reader.read()) != -1)
                    stringBuilder.append(((char) c));
                text = stringBuilder.toString();
            }
        }
        return text;
    }
}