package analyzer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

public class Tokenizer {
    public interface Predicate {
        boolean test(char c);
    }

    public static final Predicate char0 = c -> c == '.';
    public static final Predicate char1 = c -> c == '@';
    public static final Predicate char2 = c -> c == ':';
    public static final Predicate char3 = c -> c == '$';

    private final Predicate inString = new Predicate() {
        int codePrevChar = -1;
        @Override
        public boolean test(char c) {
            if (c == '\"' && codePrevChar != '\\') {
                codePrevChar = -1;
                return false;
            } else {
                codePrevChar = c;
                return true;
            }
        }
    };
    private final Predicate inChar = new Predicate() {
        int codePrevChar = -1;
        @Override
        public boolean test(char c) {
            if ((c == '’' || c == '\'') && codePrevChar != '\\') {
                codePrevChar = -1;
                return false;
            } else {
                codePrevChar = c;
                return true;
            }
        }
    };

    public static final Predicate isWhiteSpace = c -> " \t\r\f".indexOf(c) > -1;
    public static final Predicate isNotEndLine = c -> c != '\n';

    public static final Predicate isPunc = c -> "[]{}()".indexOf(c) > -1;
    public static final Predicate isOperator = c -> "+-*/%=&|<>!".indexOf(c) > -1;

    public static final Predicate isIdentifierStart = Character::isJavaIdentifierStart;
    public static final Predicate isIdentifierPart =
            c -> Character.isJavaIdentifierPart(c) || "?!".indexOf(c) > -1;

    public static final Predicate defaultPredicate =
            c -> " \t\r\f\n-=~!@#$%^&*()_+[]\\{}|;':\",./<>?".indexOf(c) > -1;

    public static final String beginComments = "=begin";
    public static final String endComments = "=end";

    private final StringBuilder code = new StringBuilder();
    private int cursor;

    public Tokenizer() {
    }
    public Tokenizer(File input) throws IOException {
        setInput(input);
    }

    private boolean canRead() {
        return cursor < code.length();
    }
    private boolean test(Predicate predicate) {
        return canRead() && predicate.test(code.charAt(cursor));
    }
    private void nextWhile(Predicate predicate) {
        while (test(predicate))
            cursor++;
    }
    private void nextDoWhile(Predicate predicate) {
        do cursor++;
        while (test(predicate));
    }

    public int line() {
        return line(cursor);
    }
    public int line(int cursor) {
        int c = code.substring(0, cursor).replaceAll("[^\n]", "").length();
        return c == 0 ? 0 : c + 1;
    }
    public int length() {
        return code.length();
    }

    public int cursor() {
        return cursor;
    }
    public void setCursor(int cursor) {
        if (cursor < 0 || cursor > code.length())
            throw new IndexOutOfBoundsException();
        this.cursor = cursor;
    }

    public void clear() {
        cursor = 0;
        code.setLength(0);
    }

    public void setInput(File input) throws IOException {
        clear();
        try (FileReader codeReader = new FileReader(input)) {
            char[] buffer = new char[1024];
            int length;
            while ((length = codeReader.read(buffer, 0, buffer.length)) > 0)
                code.append(buffer, 0, length);
        }
    }
    public void setInput(CharSequence input) {
        clear();
        code.append(input);
    }

    public String nextToken() {
        while (true) {
            nextWhile(isWhiteSpace);
            if (canRead()) {
                char ch = code.charAt(cursor);
                if (ch == '\n') {
                    cursor++;
                    return "\n";
                }
                if (ch == '#') {
                    nextDoWhile(isNotEndLine);
                    continue;
                }
                int wordEnd = cursor + beginComments.length();
                if (wordEnd < code.length() && beginComments.equals(code.substring(cursor, wordEnd))) {
                    int endCommentsIndex = code.indexOf(endComments, wordEnd);
                    cursor = endCommentsIndex < 0 ? code.length() : endCommentsIndex + endComments.length();
                    continue;
                }
                int start = cursor;
                setCursorPosition(ch);
                return code.substring(start, cursor);
            } else {
                return null;
            }
        }
    }
    private void setCursorPosition(char ch) {
        if (ch == '’' || ch == '\'') {
            nextDoWhile(inChar);
            cursor++;
        } else if (ch == '\"') {
            nextDoWhile(inString);
            cursor++;
        } else if (char0.test(ch)) {
            nextDoWhile(char0);
        } else if (char1.test(ch)) {
            nextDoWhile(c -> char1.test(c) || isIdentifierPart.test(c));
        } else if (char2.test(ch)) {
            nextDoWhile(char2);
        } else if (char3.test(ch)) {
            nextDoWhile(char3);
        } else if (isPunc.test(ch)) {
            cursor++;
        } else if (isOperator.test(ch)) {
            nextDoWhile(isOperator);
        } else if (isIdentifierStart.test(ch)) {
            nextDoWhile(isIdentifierPart);
        } else {
            if (defaultPredicate.test(ch)) {
                nextDoWhile(c -> !defaultPredicate.test(c) && !isIdentifierPart.test(c));
            } else {
                nextWhile(c -> !defaultPredicate.test(c));
            }
        }
    }

    public void forEach(Consumer<String> consumer) {
        String token;
        while ((token = nextToken()) != null)
            consumer.accept(token);
    }

    public String toString() {
        return code.toString();
    }
}