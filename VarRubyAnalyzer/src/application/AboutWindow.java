package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.*;
import java.util.Objects;

public class AboutWindow {
    @FXML
    private Label out;

    private static String text;
    private static String getText() {
        if (text == null) {
            try (Reader reader = new InputStreamReader(Objects.requireNonNull(
                    AboutWindow.class.getClassLoader().getResourceAsStream(App.ABOUT)))) {
                StringBuilder builder = new StringBuilder();
                char[] buf = new char[1024];
                int l;
                while ((l = reader.read(buf, 0, buf.length)) > 0)
                    builder.append(buf, 0, l);
                text = builder.toString();
            } catch (IOException e) {
                text = "Hello";
            }
        }
        return text;
    }

    @FXML
    private void initialize() {
        out.setText(getText());
    }
}
