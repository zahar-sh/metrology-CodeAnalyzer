package com.example.codeAnalyzer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;

public class MainWindowController {

    @FXML
    public TableView<Cell> operatorsTableView, operandsTableView;

    @FXML
    private TableColumn<Cell, String> n1, operators, operatorsCount;

    @FXML
    private TableColumn<Cell, String> n2, operands, operandsCount;

    @FXML
    private Label label;

    private final List<String> operatorsDictionary = loadDictionary();

    private final Tokenizer tokenizer = new Tokenizer();
    private final Map<String, Integer> tokenCounter = new TreeMap<>(String::compareTo);
    private final List<String> functions = new ArrayList<>();

    private final ObservableList<Cell> operatorsCells = FXCollections.observableArrayList();
    private final ObservableList<Cell> operandsCells = FXCollections.observableArrayList();

    private final FileChooser fileChooser = new FileChooser();

    private List<String> loadDictionary() {
        InputStream inputStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("file\\operatorsDictionary.txt"));
        Scanner scanner = new Scanner(inputStream);
        ArrayList<String> output = new ArrayList<>();
        while (scanner.hasNext())
            output.add(scanner.next());
        scanner.close();
        output.sort(String::compareTo);
        output.trimToSize();
        return Collections.unmodifiableList(output);
    }

    @FXML
    void initialize() {
        setUnsorted(n1, operators, operatorsCount, n2, operands, operandsCount);
        setCellFactory(n1, operators, operatorsCount);
        setCellFactory(n2, operands, operandsCount);

        setDefaultTextInLabels();

        operatorsTableView.setItems(operatorsCells);
        operandsTableView.setItems(operandsCells);
    }
    private void setUnsorted(TableColumn<?, ?>... columns) {
        for (TableColumn<?, ?> column : columns)
            column.setSortable(false);
    }
    private void setCellFactory(TableColumn<Cell, String> number,
                                TableColumn<Cell, String> value,
                                TableColumn<Cell, String> count) {
        number.setCellValueFactory(v -> v.getValue().numberProperty());
        value.setCellValueFactory(v -> v.getValue().valueProperty());
        count.setCellValueFactory(v -> v.getValue().countProperty());
    }
    @FXML
    void openFile() {
        analyze(fileChooser.showOpenDialog(null));
    }

    public void analyze(File file) {
        if (file != null) {
            try {
                handle(file);
            } catch (IOException e) {
                label.setText("Error reading the file");
            } catch (IllegalArgumentException | NullPointerException e) {
                label.setText("Code is not correct");
            }
            System.gc();
        }
    }
    private void handle(File input) throws IOException, IllegalArgumentException {
        tokenizer.setInput(input);

        tokenCounter.clear();
        functions.clear();

        String nextToken, token = tokenizer.nextToken();
        while (token != null) {
            if ("def".equals(token)) {
                increment(token);
                token = tokenizer.nextToken();
                functions.add(token);
            }
            nextToken = tokenizer.nextToken();
            if ("(".equals(nextToken)) {
                while (!")".equals(nextToken = tokenizer.nextToken()))
                    increment(nextToken);
                increment(token);
                nextToken = tokenizer.nextToken();
            } else {
                increment(token);
            }
            token = nextToken;
        }

        handlePunc("[", "]");
        handlePunc("{", "}");
        handlePunc("(", ")");
        handleBeginEnd();

        clearTable();

        var putInOperators = new BiConsumer<String, Integer>() {
            int n1 = 1;
            int N1 = 0;
            @Override
            public void accept(String key, Integer count) {
                N1 += count;
                operatorsCells.add(new Cell(n1++, key, count));
            }
        };
        var putInOperands = new BiConsumer<String, Integer>() {
            int n2 = 1;
            int N2 = 0;
            @Override
            public void accept(String key, Integer count) {
                N2 += count;
                operandsCells.add(new Cell(n2++, key, count));
            }
        };
        BiConsumer<String, Integer> splitter = (key, count) -> {
            if (functions.contains(key))
                putInOperators.accept(key + "()", count);
            else if (contains(operatorsDictionary, key))
                putInOperators.accept(key, count);
            else
                putInOperands.accept(key, count);
        };
        tokenCounter.forEach(splitter);

        putInOperators.n1--;
        putInOperands.n2--;

        int n = putInOperators.n1 + putInOperands.n2;
        int N = putInOperators.N1 + putInOperands.N2;
        int V = (int) (N * (Math.log(n) / Math.log(2)));

        operatorsCells.add(new Cell("n1 = " + putInOperators.n1, "", "N1 = " + putInOperators.N1));
        operandsCells.add(new Cell("n2 = " + putInOperands.n2, "", "N2 = " + putInOperands.N2));

        String text = "Словарь программы n = " + putInOperators.n1 + " + " + putInOperands.n2 + " = " + n + '\n';
        text += "Длина программы N = " + putInOperators.N1 + " + " + putInOperands.N2 + " = " + N + '\n';
        text += "Объем программы V = " + N + " * log(" + n + ") = " + V;
        label.setText(text);
    }

    private void increment(String key) {
        tokenCounter.put(key, tokenCounter.getOrDefault(key, 0) + 1);
    }
    private void handlePunc(String s1, String s2) {
        Integer c1 = tokenCounter.remove(s1);
        Integer c2 = tokenCounter.remove(s2);
        if (c1 != null && c2 != null) {
            if (c1.equals(c2))
                tokenCounter.put(s1 + s2, c1);
            else
                throw new IllegalArgumentException();
        }
    }
    private void handleBeginEnd() {
        Integer beginCount = tokenCounter.remove("begin");
        Integer endCount = tokenCounter.get("end");
        if (beginCount != null) {
            if (endCount == null) {
                throw new IllegalArgumentException();
            } else {
                if (endCount >= beginCount) {
                    tokenCounter.put("begin...end", beginCount);
                    int newCount = endCount - beginCount;
                    if (newCount == 0)
                        tokenCounter.remove("end");
                    else
                        tokenCounter.put("end", newCount);
                }
            }
        }
    }
    private boolean contains(List<String> list, String key) {
        return Collections.binarySearch(list, key) > -1;
    }

    @FXML
    void print() {
        printAll(fileChooser.showSaveDialog(null));
    }
    public void printAll(File output) {
        if (output != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
                writer.write("Operators:\n");
                for (Cell cell : operatorsCells)
                    writeCell(writer, cell);

                writer.write("\nOperands:\n");
                for (Cell cell : operandsCells)
                    writeCell(writer, cell);
                writer.write('\n');

                writer.append(label.getText()).write('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void writeCell(BufferedWriter writer, Cell cell) throws IOException {
        writer.append(cell.getValue()).append(" -> ").append(cell.getCount()).write('\n');
    }

    @FXML
    void clearAll() {
        clearTable();
        setDefaultTextInLabels();
    }
    private void setDefaultTextInLabels() {
        //label.setText((dictionaryText + 0) + '\n' + (lengthText + 0 )+ '\n' + (capacityText + 0));
        label.setText("Open file for analysis");
    }
    private void clearTable() {
        operatorsCells.clear();
        operandsCells.clear();
    }

    @FXML
    void info() {
        try {
            App.createWindow("infoWindow.fxml", "Info");
        } catch (IOException ignored) {
        }
    }
}