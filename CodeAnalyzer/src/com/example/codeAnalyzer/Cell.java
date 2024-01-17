package com.example.codeAnalyzer;

import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

public class Cell {
    private final SimpleStringProperty number = new SimpleStringProperty();
    private final SimpleStringProperty value = new SimpleStringProperty();
    private final SimpleStringProperty count = new SimpleStringProperty();

    public Cell() {
    }
    public Cell(String number, String value, String count) {
        setNumber(number);
        setValue(value);
        setCount(count);
    }
    public Cell(int n, String string, int count) {
        this(Integer.toString(n), string, Integer.toString(count));
    }

    public String getNumber() {
        return number.get();
    }
    public void setNumber(String number) {
        this.number.set(number);
    }
    public SimpleStringProperty numberProperty() {
        return number;
    }

    public String getValue() {
        return value.get();
    }
    public void setValue(String value) {
        this.value.set(value);
    }
    public SimpleStringProperty valueProperty() {
        return value;
    }

    public String getCount() {
        return count.get();
    }
    public void setCount(String count) {
        this.count.set(count);
    }
    public SimpleStringProperty countProperty() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return Objects.equals(number, cell.number) &&
                Objects.equals(value, cell.value) &&
                Objects.equals(count, cell.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, value, count);
    }

    @Override
    public String toString() {
        return "Cell{number=" + number + ", value=" + value + ", count=" + count + '}';
    }
}