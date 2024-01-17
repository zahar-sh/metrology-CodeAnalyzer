package analyzer;

import java.util.*;

public class Analyzer {
    private static List<String> operators;
    public static List<String> getOperatorsList() {
        if (operators == null) {
            ArrayList<String> out = new ArrayList<>();
            Scanner scanner = new Scanner(Objects.requireNonNull(Analyzer.class.getClassLoader().
                    getResourceAsStream(Util.OPERATORS_DICTIONARY)));
            while (scanner.hasNext())
                out.add(scanner.next());
            scanner.close();
            out.trimToSize();
            out.sort(String::compareTo);
            operators = Collections.unmodifiableList(out);
        }
        return operators;
    }

    private final List<Map.Entry<Token, Integer>> nestingEntryList = new ArrayList<>();
    private final NestingAnalyzer analyzer = new NestingAnalyzer();
    private final StringBuilder out = new StringBuilder();

    public String analyze(Tokenizer tokenizer) {
        analyzer.analyze(tokenizer, nestingEntryList);
        for (Map.Entry<Token, Integer> entry : nestingEntryList)
            out.append(entryToString(entry)).append('\n');
        int allOperatorsCount = 0;
        int ifCount = 0;
        tokenizer.setCursor(0);
        Token temp;
        while ((temp = tokenizer.nextToken()).string != null)
            if (Collections.binarySearch(getOperatorsList(), temp.string) > -1)
                allOperatorsCount++;
        for (Map.Entry<Token, Integer> e : nestingEntryList) {
            String string = e.getKey().string;
            switch (string) {
                case "if":
                case "else if":
                case "for":
                case "while":
                case "case":
                    ifCount++;
                    break;
            }
        }
        out.append("\nAll operators: ").append(allOperatorsCount).append('\n');
        out.append("If operators: ").append(ifCount).append('\n');
        double cl = allOperatorsCount == 0 ? 0 :
                (Math.round(ifCount * 1000.0 / allOperatorsCount) / 1000.0);
        out.append("cl: ").append(cl).append('\n');

        Map.Entry<Token, Integer> max = maxNestingOf(nestingEntryList);
        if (max == null) {
            out.append("Max nesting token not found").append('\n');
        } else {
            out.append("Max: ").append(entryToString(max)).append('\n');
        }
        nestingEntryList.clear();
        String result = out.toString();
        out.setLength(0);
        return result;
    }

    private Map.Entry<Token, Integer> maxNestingOf(List<Map.Entry<Token, Integer>> list) {
        Map.Entry<Token, Integer> max = null;
        for (Map.Entry<Token, Integer> e : list) {
            if (!"def".equals(e.getKey().string))
                if (max == null || e.getValue().compareTo(max.getValue()) > 0)
                    max = e;
        }
        return max;
    }
    private String entryToString(Map.Entry<Token, Integer> e) {
        return e.getKey().string + "->" + e.getValue().toString();
    }
}