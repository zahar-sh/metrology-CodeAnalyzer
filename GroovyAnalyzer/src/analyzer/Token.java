package analyzer;

import java.util.Objects;

public class Token {
    public static final Token EMPTY_TOKEN = new Token(null, -1);
    public final String string;
    public final int index;

    public Token(String string, int index) {
        this.string = string;
        this.index = index;
    }

    public int hashCode() {
        return index;
    }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return index == token.index &&
                Objects.equals(string, token.string);
    }
    public String toString() {
        return string;
    }
}