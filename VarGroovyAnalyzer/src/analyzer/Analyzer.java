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


    private static String handleDots(Tokenizer tokenizer, Block block, String beforeDot) {
        makeUsedIfContains(block, beforeDot);
        return skipDots(tokenizer, tokenizer.nextToken());
    }
    private static String skipDots(Tokenizer tokenizer, String token) {
        while (isWord(token)) {
            String next = tokenizer.nextToken();
            if (next.equals(".")) {
                token = tokenizer.nextToken();
            } else {
                return next;
            }
        }
        return token;
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
        while ((token = next) != null) {
            next = tokenizer.nextToken();
            if ("else".equals(token) && "if".equals(next)) {
                token = "else if";
                next = tokenizer.nextToken();
            } else if (isWord(token) && "(".equals(next) && "class".equals(block.start)) {
                block = openBlock(block, token);
                next = tokenizer.nextToken();
                if (!")".equals(next)) {
                    while (true) {
                        token = next;
                        next = tokenizer.nextToken();
                        if (next.equals(",")) {
                            block.vars.add(new Variable(token));
                            next = tokenizer.nextToken();
                        } else if (next.equals(")")) {
                            block.vars.add(new Variable(token));
                            break;
                        }
                    }
                }
                tokenizer.nextToken();
                token = next;
                next = tokenizer.nextToken();
            }
            switch (token) {
                case "case":
                case "else if":
                case "else":
                    block = closeBlock(block, token);
                    block = openBlock(block, token);
                    break;
                case "class":
                case "switch":
                case "if":
                case "for":
                case "while":
                case "do":
                    block = openBlock(block, token);
                    break;
                case "}":
                    if (!"else".equals(next))
                        block = closeBlock(block, token);
                    break;
                default:
                    if ("print".equals(token) || "println".equals(token)) {
                        if ("(".equals(next))
                            next = readBraces(tokenizer, block, Variable.OUTPUT);
                        else if (isWord(next))
                            createOrAppend(block, next, Variable.OUTPUT);
                    } else if (next != null) {
                        if (".".equals(next)) {
                            next = handleDots(tokenizer, block, token);
                        } else if ("not".equals(token) || "!".equals(token)) {
                            next = checkNext(tokenizer, block, next, Variable.CONTROLLED);
                        } else {
                            label:
                            if (isWord(token)) {
                                switch (next) {
                                    case "(":
                                        next = readBraces(tokenizer, block, Variable.USED);
                                        continue;
                                    case "[":
                                        createOrAppend(block, token, Variable.USED);
                                        token = next;
                                        next = tokenizer.nextToken();
                                        if (next.equals("]")) {
                                            next = tokenizer.nextToken();
                                            continue;
                                        }
                                        break label;
                                    case ",":
                                    case ";":
                                        createOrAppend(block, token, Variable.UNUSED);
                                        break;
                                    case "=":
                                        createOrAppend(block, token, Variable.UNUSED, Variable.MODIFIED);

                                        String t = tokenizer.nextToken(); //after =
                                        if (isWord(t)) {
                                            makeUsedIfContains(block, t);
                                            next = tokenizer.nextToken();
                                            if (next.equals(".")) {
                                                next = tokenizer.nextToken();
                                                if (next.equalsIgnoreCase("readline"))
                                                    createOrAppend(block, token, Variable.INPUT);
                                                else {
                                                    next = skipDots(tokenizer, next);
                                                }
                                            }
                                        }
                                        continue;
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
                                        continue;
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

    private static String readBraces(Tokenizer tokenizer, Block block, int flag) {
        String token = tokenizer.nextToken();
        if (!token.equals(")")) {
            String next;
            do {
                next = tokenizer.nextToken();
                if (isWord(token)) {
                    if (next.equals(".")) {
                        next = handleDots(tokenizer, block, token);
                    } else {
                        createOrAppend(block, token, flag);
                    }
                }
                token = next;
                if (token.equals(","))
                    token = tokenizer.nextToken();
            } while (!token.equals(")"));
        }
        return token;
    }
}