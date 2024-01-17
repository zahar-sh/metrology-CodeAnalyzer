package com.example.codeAnalyzer;

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

    private static final Predicate inString = new Predicate() {
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
    private static final Predicate inChar = new Predicate() {
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

    public static final Predicate isWhiteSpace = c -> " \t\r\f\n".indexOf(c) > -1;
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

    public void setInput(File input) throws IOException {
        cursor = 0;
        code.setLength(0);
        try (FileReader codeReader = new FileReader(input)) {
            int codeChar;
            while ((codeChar = codeReader.read()) != -1)
                code.append(((char) codeChar));
        }
    }

    public String nextToken() {
        while (true) {
            nextWhile(isWhiteSpace);
            if (canRead()) {
                char ch = code.charAt(cursor);
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
                cursor++;
                nextWhile(c -> !defaultPredicate.test(c) && !isIdentifierPart.test(c));
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
}