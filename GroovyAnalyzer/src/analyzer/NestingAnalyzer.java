package analyzer;

import java.util.*;

public class NestingAnalyzer {
    private Tokenizer tokenizer;
    private final List<Token> tokens = new ArrayList<>();
    private final Map<Token, Integer> nestingCounter = new HashMap<>();
    private final Deque<Token> children = new ArrayDeque<>();

    public void analyze(Tokenizer tokenizer, List<Map.Entry<Token, Integer>> out) {
        this.tokenizer = Objects.requireNonNull(tokenizer);
        try {
            analyze();
        } catch (Exception ignored) {
        }
        out.addAll(nestingCounter.entrySet());
        out.sort(Comparator.comparingInt(o -> o.getKey().index));
        clear();
    }
    private void analyze() {
        tokenizer.forEach(token -> {
            switch (token.string) {
                case "if":
                    int last = tokens.size() - 1;
                    if (last != -1) {
                        Token prev = tokens.get(last);
                        if (prev.string.equals("else")) {
                            tokens.remove(prev);
                            token = new Token("else if", prev.index);
                        }
                    }
                case "else":

                case "for":
                case "while":

                case "switch":
                case "case":
                case "default":

                case "}":
                    tokens.add(token);
                    break;
            }
        });

        Iterator<Token> iterator = tokens.iterator();
        Token next = nextOrNull(iterator);
        Token token;
        while ((token = next) != null) {
            next = nextOrNull(iterator);
            if ("}".equals(token.string)) {
                if (next != null)
                    if ("else if".equals(next.string) || "else".equals(next.string))
                        continue;
                Token parent = children.pop();
                switch (parent.string) {
                    case "if":
                    case "switch":
                    case "for":
                    case "while":
                        setParentNesting(nestingCounter.get(parent) + 1);
                        break;
                    case "else if":
                        temp(0, nestingCounter.get(parent) + 1, "if", "else if");
                        break;
                    case "else":
                        onElse(parent, "if", "else if");
                        break;

                    case "case":
                        temp(0, nestingCounter.get(parent), "switch", "case");
                        break;
                    case "default":
                        onElse(parent, "switch", "case");
                        break;
                }
            } else {
                children.push(token);
                nestingCounter.put(token, 0);
            }
        }
    }

    private void onElse(Token parent, String If, String elseIf) {
        int elseNest = nestingCounter.get(parent);
        Token elseParent = children.pop();
        if (If.equals(elseParent.string)) {
            nestingCounter.put(elseParent, elseNest);
            setParentNesting(elseNest + 1);
        } else if (elseIf.equals(elseParent.string)) {
            int nest = nestingCounter.get(elseParent);
            if ("if".equals(If)) nest++;
            temp(elseNest, nest, If, elseIf);
        }
    }

    private void temp(int elseNest, int nestingIf, String If, String ElseIf) {
        boolean loop = true;
        do {
            Token parent = children.pop();
            String s = parent.string;
            if (If.equals(s)) {
                if (elseNest > nestingIf)
                    nestingIf = elseNest;
                nestingCounter.put(parent, nestingIf);
                setParentNesting(nestingIf + 1);
                loop = false;
            } else if (ElseIf.equals(s)) {
                nestingIf += nestingCounter.get(parent) + 1;
            } else {
                throw new IllegalStateException("Unexpected value: " + s);
            }
        } while (loop);
    }
    private void setParentNesting(int nesting) {
        Token parent = children.peek();
        if (parent != null && nesting > nestingCounter.get(parent))
            nestingCounter.put(parent, nesting);
    }
    private void clear() {
        tokens.clear();
        nestingCounter.clear();
        children.clear();
    }

    private static Token nextOrNull(Iterator<Token> iterator) {
        return iterator.hasNext() ? iterator.next() : null;
    }
}