package application;

import javafx.beans.property.SimpleStringProperty;

class IdentifierCell {
    private final SimpleStringProperty identifier = new SimpleStringProperty();
    private final SimpleStringProperty spen = new SimpleStringProperty();

    public IdentifierCell() {
    }

    public IdentifierCell(String identifier, String spen) {
        setIdentifier(identifier);
        setSpen(spen);
    }

    public IdentifierCell(String token, int spen) {
        this(token, Integer.toString(spen));
    }

    public String getIdentifier() {
        return identifier.get();
    }

    public void setIdentifier(String identifier) {
        this.identifier.set(identifier);
    }

    public SimpleStringProperty identifierProperty() {
        return identifier;
    }


    public String getSpen() {
        return spen.get();
    }

    public void setSpen(String spen) {
        this.spen.set(spen);
    }

    public SimpleStringProperty spenProperty() {
        return spen;
    }
}
