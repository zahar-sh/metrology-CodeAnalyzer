package analyzer;

import java.util.*;

public class NestingAnalyzer {
    static {
        ArrayList<String> out = new ArrayList<>();
        Collections.addAll(out,
                "def", "begin", "end",
                "if", "elsif", "else", "unless",
                "for", "while", "do", "loop", "until",
                "case", "when");
        out.trimToSize();
        out.sort(String::compareTo);
        OPERATORS = Collections.unmodifiableList(out);
    }
    public static final List<String> OPERATORS;
    public static boolean isOperator(String key) {
        return Collections.binarySearch(OPERATORS, key) > -1;
    }

    private Tokenizer tokenizer;
    private final List<Token> tokens = new ArrayList<>();
    private final Map<Token, Integer> nestingCounter = new HashMap<>();
    private final Deque<Token> children = new ArrayDeque<>();

    public void analyze(Tokenizer tokenizer, List<Map.Entry<Token, Integer>> out) {
        this.tokenizer = Objects.requireNonNull(tokenizer);
        readOperators();
        try {
            analyze();
        } catch (Exception ignored) {
        }
        out.addAll(nestingCounter.entrySet());
        out.sort(Comparator.comparingInt(o -> o.getKey().index));
        clear();
    }

    private void readOperators() {
        tokenizer.forEach(token -> {
            if (isOperator(token.string))
                tokens.add(token);
        });
        Iterator<Token> iterator = tokens.iterator();
        if (iterator.hasNext()) {
            Token prev = iterator.next();
            while (iterator.hasNext()) {
                Token token = iterator.next();
                switch (prev.string) {
                    case "begin":
                    case "end":
                    case "for":
                    case "while":
                    case "loop":
                    case "until":
                        if ("do".equals(token.string))
                            if (tokenizer.line(token.index) == tokenizer.line(prev.index))
                                iterator.remove();
                }
                prev = token;
            }
        }
    }
    private void analyze() {
        for (Token token : tokens) {
            if ("end".equals(token.string)) {
                Token parent = children.pop();
                switch (parent.string) {
                    case "loop":
                        setParent(nestingCounter.get(parent));
                        break;
                    case "if":
                    case "unless":

                    case "for":
                    case "while":
                    case "do":
                    case "until":

                    case "def":
                    case "begin":
                        setParent(nestingCounter.get(parent) + 1);
                        break;
                    case "elsif":
                        temp(0, nestingCounter.get(parent) + 1, "if", "elsif");
                        break;
                    case "else":
                        onElse(parent);
                        break;
                    case "case":
                        break;
                    case "when":
                        temp(0, nestingCounter.get(parent), "case", "when");
                        break;
                }
            } else {
                children.push(token);
                nestingCounter.put(token, 0);
            }
        }
    }
    private void onElse(Token Else) {
        int ElseNest = nestingCounter.get(Else);
        Token elseParent = children.pop();
        switch (elseParent.string) {
            case "if":
                nestingCounter.put(elseParent, ElseNest);
                setParent(ElseNest + 1);
                break;
            case "elsif":
                temp(ElseNest, nestingCounter.get(elseParent) + 1, "if", "elsif");
                break;
            case "when":
                temp(ElseNest, nestingCounter.get(elseParent), "case", "when");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + elseParent.string);
        }
    }
    private void temp(int elseNest, int nestingIf, String IF, String ELSIF) {
        boolean loop = true;
        do {
            Token parent = children.pop();
            if (IF.equals(parent.string)) {
                if (elseNest > nestingIf)
                    nestingIf = elseNest;
                nestingCounter.put(parent, nestingIf);
                setParent(nestingIf + 1);
                loop = false;
            } else if (ELSIF.equals(parent.string)) {
                nestingIf += nestingCounter.get(parent) + 1;
            } else {
                throw new IllegalStateException("Unexpected value: " + parent.string);
            }
        } while (loop);
    }
    private void setParent(int newVal) {
        Token ifParent = children.peek();
        if (ifParent != null && newVal > nestingCounter.get(ifParent))
            nestingCounter.put(ifParent, newVal);
    }
    private void clear() {
        tokens.clear();
        nestingCounter.clear();
        children.clear();
    }
}