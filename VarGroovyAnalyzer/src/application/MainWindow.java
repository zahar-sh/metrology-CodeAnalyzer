package application;

import analyzer.Analyzer;
import analyzer.Block;
import analyzer.Tokenizer;
import analyzer.Variable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

public class MainWindow {
    private final ObservableList<Cell> allCells = FXCollections.observableArrayList();
    private final VarFilter all = new VarFilter(allCells);
    @FXML
    private TableView<Cell> allTable;
    @FXML
    private TableColumn<Cell, String> p, c, m, t;

    private final ObservableList<Cell> inOutCells = FXCollections.observableArrayList();
    private final VarFilter inOut = new VarFilter(inOutCells);
    @FXML
    private TableView<Cell> inOutTable;
    @FXML
    private TableColumn<Cell, String> p1, c1, m1, t1;


    private final ObservableList<IdentifierCell> identifierCells = FXCollections.observableArrayList();
    @FXML
    private TableView<IdentifierCell> identifierTable;
    @FXML
    private TableColumn<IdentifierCell, String> identifier, spen;

    @FXML
    private Label out;
    @FXML
    private TextArea code;

    private final Tokenizer tokenizer = new Tokenizer();
    private final FileChooser fileChooser = new FileChooser();
    private Stage stage = null;

    @FXML
    private void initialize() throws IOException {
        allTable.setItems(allCells);
        p.setCellValueFactory(n -> n.getValue().pProperty());
        c.setCellValueFactory(n -> n.getValue().cProperty());
        m.setCellValueFactory(n -> n.getValue().mProperty());
        t.setCellValueFactory(n -> n.getValue().tProperty());

        inOutTable.setItems(inOutCells);
        p1.setCellValueFactory(n -> n.getValue().pProperty());
        c1.setCellValueFactory(n -> n.getValue().cProperty());
        m1.setCellValueFactory(n -> n.getValue().mProperty());
        t1.setCellValueFactory(n -> n.getValue().tProperty());

        identifierTable.setItems(identifierCells);
        identifier.setCellValueFactory(n -> n.getValue().identifierProperty());
        spen.setCellValueFactory(n -> n.getValue().spenProperty());

        clear();
        File input = new File("code.txt");
        StringBuilder sb = new StringBuilder();
        try (FileReader codeReader = new FileReader(input)) {
            char[] buffer = new char[1024];
            int length;
            while ((length = codeReader.read(buffer, 0, buffer.length)) > 0)
                sb.append(buffer, 0, length);
        }
        String s = sb.toString();
        code.setText(s);
        tokenizer.setInput(s);
        run();
    }

    private void run() {
        final Block module;
        try {
            module = Analyzer.analyze(tokenizer);
        } catch (Exception e) {
            out.setText("Error analyzing: " + e.getMessage());
            return;
        }
        allCells.clear();
        inOutCells.clear();
        identifierCells.clear();
        all.reset();
        inOut.reset();

        var consumer = new Consumer<Variable>() {
            int totalSpen = 0;
            @Override
            public void accept(Variable var) {
                totalSpen += var.spen;
                identifierCells.add(new IdentifierCell(var.token, var.spen));
                if (var.hasFlag(Variable.INPUT) || var.hasFlag(Variable.OUTPUT))
                    inOut.add(var);
                all.add(var);
            }
        };
        module.forEachVar(consumer);
        allCells.add(0, total(all));
        inOutCells.add(0, total(inOut));
        identifierCells.add(0, new IdentifierCell("", "total = " + consumer.totalSpen));
        out.setText("Метрика Чепина: \n" + result(all) + "\nМетрика Чепина (in/out): \n" + result(inOut));
        allTable.refresh();
        inOutTable.refresh();
        identifierTable.refresh();

        System.out.println(Analyzer.print(module)); /////////////////////////////////////
    }

    private String result(VarFilter all) {
        double result = all.p + (all.m << 1) + (3 * all.c) + (all.t / 2.0);
        return "Q = " + all.p + " + 2*" + all.m + " + 3*" + all.c + " + 0,5*" + all.t + " = " + result;
    }
    private Cell total(VarFilter all) {
        return new Cell("size = " + all.p, "size = " + all.c,
                "size = " + all.m, "size = " + all.t);
    }

    @FXML
    private void analyze() {
        tokenizer.setInput(code.getText());
        run();
    }

    @FXML
    private void openFile() {
        File input = fileChooser.showOpenDialog(null);
        if (input != null) {
            try {
                tokenizer.setInput(input);
                run();
            } catch (IOException ignored) {
            }
        }
    }

    @FXML
    private void clear() {
        code.setText("");
        out.setText("");

        allCells.clear();
        allTable.refresh();

        inOutCells.clear();
        inOutTable.refresh();

        identifierCells.clear();
        identifierTable.refresh();
    }

    @FXML
    private void createAboutWindow() {
        if (stage == null) {
            try {
                Stage st = new Stage();
                st.setScene(new Scene(FXMLLoader.load(getClass().
                        getResource("about_window.fxml"))));
                st.setTitle("About");
                st.setResizable(false);
                st.getIcons().add(App.ICON);
                st.setOnCloseRequest(windowEvent -> stage = null);
                st.show();
                stage = st;
            } catch (IOException ignored) {
                stage = null;
            }
        }
    }
}