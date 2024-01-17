package application;

import javafx.beans.property.SimpleStringProperty;

class Cell {
    private final SimpleStringProperty p = new SimpleStringProperty();
    private final SimpleStringProperty c = new SimpleStringProperty();
    private final SimpleStringProperty m = new SimpleStringProperty();
    private final SimpleStringProperty t = new SimpleStringProperty();

    public Cell() {
    }

    public Cell(String p, String c, String m, String t) {
        setP(p);
        setC(c);
        setM(m);
        setT(t);
    }

    public String getP() {
        return p.get();
    }

    public void setP(String p) {
        this.p.set(p);
    }

    public SimpleStringProperty pProperty() {
        return p;
    }

    public String getC() {
        return c.get();
    }

    public void setC(String c) {
        this.c.set(c);
    }

    public SimpleStringProperty cProperty() {
        return c;
    }

    public String getM() {
        return m.get();
    }

    public void setM(String m) {
        this.m.set(m);
    }

    public SimpleStringProperty mProperty() {
        return m;
    }

    public String getT() {
        return t.get();
    }

    public void setT(String t) {
        this.t.set(t);
    }

    public SimpleStringProperty tProperty() {
        return t;
    }

}
