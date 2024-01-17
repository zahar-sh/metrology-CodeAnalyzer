package analyzer;

public class Analyzer {
    private static StringBuilder appendVar(StringBuilder sb, Variable var) {
        if (var.hasFlag(Variable.CONTROLLED))
            sb.append("CONTROLLED, ");
        if (var.hasFlag(Variable.MODIFIED))
            sb.append("MODIFIED, ");
        if (var.hasFlag(Variable.USED))
            sb.append("USED, ");
        if (var.hasFlag(Variable.INPUT))
            sb.append("INPUT, ");
        if (var.hasFlag(Variable.OUTPUT))
            sb.append("OUTPUT, ");
        sb.append("spen=").append(var.spen);
        return sb;
    }
    public static String print(Block module) {
        StringBuilder sb = new StringBuilder();
        sb.append("START\n");
        for (Block child : module.children)
            print(child, "\t", sb);
        for (Variable var : module.vars) {
            sb.append(var.token).append("=");
            appendVar(sb, var).append('\n');
        }
        sb.append("END");
        return sb.toString();
    }
    private static void print(Block block, String space, StringBuilder sb) {
        sb.append(space).append(block.start).append('\n');
        for (Variable var : block.vars) {
            sb.append(space).append(var.token).append("=");
            appendVar(sb, var).append('\n');
        }
        for (Block child : block.children)
            print(child, space + '\t', sb);
        String end = block.end;
        if (!("case".equals(end) || "else if".equals(end) || "else".equals(end)))
            sb.append(space).append(end).append('\n');
    }

    private static boolean isWord(String token) {
        return token != null && Tokenizer.isIdentifierStart.test(token.charAt(0));
    }

    private static void createOrAppend(Block currentBlock, String token, int all) {
        createOrAppend(currentBlock, token, all, all);
    }
    private static void createOrAppend(Block currentBlock, String token, int creation, int append) {
        if ("false".equals(token) ||
                "true".equals(token))
            return;
        Block block = currentBlock.hasVar(token);
        if (block == null) {
            currentBlock.vars.add(new Variable(token, creation));
        } else {
            Variable var = block.varOf(token);
            var.addFlag(append);
            var.spen++;
        }
    }

    private static void makeUsedIfContains(Block block, String token) {
        Block b = block.hasVar(token);
        if (b != null) {
            Variable var = b.varOf(token);
            var.addFlag(Variable.USED);
            var.spen++;
        }
    }
    private static void readString(String string, Block block) {
        string = string.substring(1, string.length() - 1);
        Tokenizer tokenizer = new Tokenizer();
        String token, next;
        int st, en = 0;
        while (!((st = string.indexOf("#{", en)) < 0 ||
                (en = string.indexOf("}", st)) < 0)) {
            tokenizer.setInput(string.substring(st + 2, en));
            next = tokenizer.nextToken();
            while ((token = next) != null) {
                next = tokenizer.nextToken();
                if (next != null) {
                    if (".".equals(next)) {
                        next = handleDots(tokenizer, block, token);
                        continue;
                    }
                    if (isWord(next))
                        next = operators(block, tokenizer, token, next);
                }
                if (isWord(token))
                    makeUsedIfContains(block, token);
            }
        }
    }

    private static boolean isNotEmptyString(String string) {
        return string.length() > 2 && '"' == string.charAt(0);
    }
    private static boolean isString(String string) {
        return !string.isEmpty() && '"' == string.charAt(0);
    }

    private static String handleDots(Tokenizer tokenizer, Block block, String beforeDot) {
        makeUsedIfContains(block, beforeDot);
        beforeDot = tokenizer.nextToken();
        while (isWord(beforeDot)) {
            String next = tokenizer.nextToken();
            if (next.equals(".")) {
                beforeDot = tokenizer.nextToken();
            } else {
                return next;
            }
        }
        return beforeDot;
    }
    private static String checkNext(Tokenizer tokenizer, Block block, String token, int all) {
        return checkNext(tokenizer, block, token, all, all);
    }
    private static String checkNext(Tokenizer tokenizer, Block block, String token, int creation, int append) {
        String next = tokenizer.nextToken();
        if (next == null) {
            makeUsedIfContains(block, token);
        } else {
            if (".".equals(next)) {
                return handleDots(tokenizer, block, token);
            } else if (!("[".equals(next) || "{".equals(next))) {
                createOrAppend(block, token, creation, append);
            }
        }
        return next;
    }
    private static String operators(Block block, Tokenizer tokenizer, String token, String next) {
        switch (token) {
            case "+":
            case "-":
            case "%":
            case "&":
            case "&&":
            case "*":
            case "**":
            case "/":
            case "<<":
            case ">>":
            case "^":
            case "|":
            case "~":

            case "[":
            case "=":
            case "+=":
            case "-=":
            case "%=":
            case "&=":
            case "&&=":
            case "*=":
            case "**=":
            case "/=":
            case "<<=":
            case ">>=":
            case "|=":
            case "||=":
                next = checkNext(tokenizer, block, next, Variable.USED);
                break;

            case "!=":
            case "<":
            case "<=":
            case "<=>":
            case "==":
            case "===":
            case ">":
            case ">=":
            case "||":
            case "and":
            case "or":
                next = checkNext(tokenizer, block, next, Variable.CONTROLLED);
                break;
        }
        return next;
    }
    private static String readDef(Tokenizer tokenizer, Block currentBlock, String token) {
        String next;
        while (!token.equals("("))
            token = tokenizer.nextToken();
        token = tokenizer.nextToken();
        while (!token.equals(")")) {
            currentBlock.vars.add(new Variable(token));
            token = tokenizer.nextToken();
            if (token.equals(","))
                token = tokenizer.nextToken();
        }
        tokenizer.nextToken();
        next = tokenizer.nextToken();
        return next;
    }

    private static Block openBlock(Block currentBlock, String token) {
        Block block = new Block(currentBlock, token);
        currentBlock.children.add(block);
        currentBlock = block;
        return currentBlock;
    }
    private static Block closeBlock(Block currentBlock, String token) {
        currentBlock.end = token;
        currentBlock = currentBlock.parent;
        return currentBlock;
    }

    public static Block analyze(Tokenizer tokenizer) {
        Block module = new Block(null, null);
        Block block = module;
        String token, next = tokenizer.nextToken();
        loop:
        while ((token = next) != null) {
            next = tokenizer.nextToken();
            switch (token) {
                case "when":
                case "elsif":
                case "else":
                    block = closeBlock(block, token);
                    block = openBlock(block, token);
                    break;
                case "def":
                    /*block = openBlock(block, token);
                    next = readDef(tokenizer, block, token);*/
                    block = openBlock(block, token + " " + next);
                    next = readDef(tokenizer, block, next);
                    break;
                case "case":
                case "begin":
                case "if":
                case "unless":
                case "for":
                case "while":
                case "do":
                case "until":
                    block = openBlock(block, token);
                    break;
                case "end":
                    block = closeBlock(block, token);
                    break;
                default:
                    if (isNotEmptyString(token))
                        readString(token, block);
                    else if ("print".equals(token) || "puts".equals(token)) {
                        if (isWord(next))
                            next = checkNext(tokenizer, block, next, Variable.OUTPUT);
                    } else if (next != null) {
                        if (".".equals(next)) {
                            next = handleDots(tokenizer, block, token);
                        } else if ("not".equals(token) || "!".equals(token)) {
                            next = checkNext(tokenizer, block, next, Variable.CONTROLLED);
                        } else {

                            if (isWord(token)) {
                                switch (next) {
                                    case "(":
                                        next = tokenizer.nextToken();
                                        if (!next.equals(")")) {
                                            do {
                                                String n = tokenizer.nextToken();
                                                if (isWord(next)) {
                                                    if (n.equals(".")) {
                                                        n = handleDots(tokenizer, block, next);
                                                    } else {
                                                        createOrAppend(block, next, Variable.USED);
                                                    }
                                                }
                                                next = n;
                                                if (next.equals(",")) {
                                                    next = tokenizer.nextToken();
                                                }
                                            } while (!next.equals(")"));
                                        }
                                        continue loop;
                                    case "[":
                                    case "{":
                                        createOrAppend(block, token, Variable.USED);
                                        break;
                                    case ",":
                                    case ";":
                                        createOrAppend(block, token, Variable.UNUSED);
                                        break;
                                    case "=":
                                        String t = tokenizer.nextToken();
                                        if ("readline".equals(t) || "gets".equals(t)) {
                                            createOrAppend(block, token,
                                                    Variable.UNUSED | Variable.INPUT,
                                                    Variable.MODIFIED | Variable.INPUT);
                                            next = tokenizer.nextToken();
                                            continue loop;
                                        } else {
                                            createOrAppend(block, token, Variable.UNUSED, Variable.MODIFIED);
                                            next = tokenizer.nextToken();

                                            if (next.equals("."))
                                                next = handleDots(tokenizer, block, t);
                                            else if (isWord(t))
                                                createOrAppend(block, t, Variable.USED);
                                        }
                                        break;
                                    case "+=":
                                    case "-=":
                                    case "++":
                                    case "--":
                                    case "%=":
                                    case "&=":
                                    case "&&=":
                                    case "*=":
                                    case "**=":
                                    case "/=":
                                    case "<<=":
                                    case ">>=":

                                    case "|=":
                                    case "||=":
                                        createOrAppend(block, token, Variable.MODIFIED);
                                        break;
                                    case "!=":
                                    case "<":
                                    case "<=":
                                    case "<=>":
                                    case "==":
                                    case "===":
                                    case ">":
                                    case ">=":
                                    case "|":
                                    case "||":
                                    case "~":
                                    case "and":
                                    case "or":
                                        createOrAppend(block, token, Variable.CONTROLLED);
                                        break;
                                    default:
                                        continue loop;
                                }
                                token = next;
                                next = tokenizer.nextToken();
                            }
                            if (isWord(next))
                                next = operators(block, tokenizer, token, next);
                        }
                    }
            }
        }
        return module;
    }
}