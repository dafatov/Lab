import java.util.*;

class Parser {
    //+ lang -> expression*
    //+ expression -> init|forModule|ifModule|assign|setAssign
    //+ init -> TYPE VAR SEMICOLON
    //+ assign -> assignOperation SEMICOLON
    //+ assignOperation -> VAR ASSIGN_OP value
    //+ value -> val valueOperation*
    //+ valueOperation -> OP val
    //+ val -> VAR|DIGIT|breakValue
    //+ breakValue -> PARENTHESIS_LEFT value PARENTHESIS_RIGHT
    //+ ifModule -> IF exprIf body (ELSE body)?
    //+ exprIf -> PARENTHESIS_LEFT setContains|setIsEmpty|logExpr PARENTHESIS_RIGHT
    //+ body -> BRACE_LEFT init|assign|setAssign BRACE_RIGHT
    //+ logExpr -> assignOperation|value LOG_OP assignOperation|value
    //+ forModule -> FOR exprFor body
    //+ exprFor -> PARENTHESIS_LEFT assign logExprFor assignOperation PARENTHESIS_RIGHT
    //+ logExprFor -> setContains|setIsEmpty|logExpr SEMICOLON
    //+ setAssign -> setAdd|setRemove|setClear
    //+ setContains -> VAR CONTAINS value
    //+ setIsEmpty -> VAR IS_EMPTY
    //+ setAdd -> VAR ADD value SEMICOLON
    //+ setRemove -> VAR REMOVE value SEMICOLON
    //+ setClear -> VAR CLEAR SEMICOLON

    Map<String, Object[]> tableOfVariables = new HashMap<>();//[var, [type, value]]
    List<String> tokensRPN = new ArrayList<>();
    private Stack<String> stack = new Stack<>();
    private List<Token> tokens = new ArrayList<>();
    private int position = 0;
    private int p1;
    private int p2;

    boolean lang(List<Token> tokens) {
        boolean lang = false;
        int majorTokens = 1;

        for (Token token : tokens) {
            if (token.getLexeme() != Lexeme.WS) {
                this.tokens.add(token);
            }
        }
        while (this.tokens.size() != position) {

            if (!expression()) {
                System.err.println("Error: ErrorSyntax in majorToken: " + majorTokens);
                System.exit(4);
            } else {
                majorTokens++;
                lang = true;
            }
        }

        return lang;
    }

    private boolean expression() {
        return init() || assign() || setAssign() || ifModule() || forModule();
    }

    private boolean assign() {
        boolean assign = false;
        int zip = position;

        if (assignOperation()) {
            if (getCurrentTokenLexemeInc() == Lexeme.SEMICOLON) {
                assign = true;
            }
        }
        position = assign ? position : zip;
        return assign;
    }

    private boolean assignOperation() {
        boolean assignOperation = false;
        int zip = position;
        boolean add = false;
        String op, var;

        if (getCurrentTokenLexemeInc() == Lexeme.VAR) {
            add = tokensRPN.add(getLastTokenValue());
            var = getLastTokenValue();

            if (getCurrentTokenLexemeInc() == Lexeme.ASSIGN_OP) {
                if (!tableOfVariables.get(var)[0].equals("set")) {
                    op = getLastTokenValue();
                    if (value()) {
                        while (!stack.empty()) {
                            tokensRPN.add(stack.pop());
                        }
                        tokensRPN.add(op);
                        assignOperation = true;
                    }
                } else {
                    System.err.println("Error: Try to assign set variable");
                    System.exit(666);
                }
            }
        }
        if (!assignOperation) {
            position = zip;
            if (add) {
                tokensRPN.remove(tokensRPN.size() - 1);
            }
        }
        return assignOperation;
    }

    private boolean init() {
        boolean init = false;
        int zip = position;
        String type, var;

        if (getCurrentTokenLexemeInc() == Lexeme.TYPE) {
            type = getLastTokenValue();
            if (getCurrentTokenLexemeInc() == Lexeme.VAR) {
                var = getLastTokenValue();
                if (getCurrentTokenLexemeInc() == Lexeme.SEMICOLON) {
                    if (type.equals("set")) {
                        tableOfVariables.put(var, valueCreate(type, new MyHashSet()));
                    } else {
                        tableOfVariables.put(var, valueCreate(type, ""));
                    }
                    init = true;
                }
            }
        }
        position = init ? position : zip;
        return init;
    }

    private boolean value() {
        if (val()) {
            while (valueOperation()) {
            }
            return true;
        }
        return false;
    }

    private boolean valueOperation() {
        boolean valueOperation = false;
        int zip = position;

        if (getCurrentTokenLexemeInc() == Lexeme.ARITHMETIC_OP) {
            String arithmeticOP = getLastTokenValue();
            //
            if (!stack.empty()) {
                while (getPriority(arithmeticOP) <= getPriority(stack.peek())) {
                    tokensRPN.add(stack.pop());
                    if (stack.empty()) {
                        break;
                    }
                }
            }
            //
            stack.push(arithmeticOP);
            if (val()) {
                valueOperation = true;
            }
        }
        position = valueOperation ? position : zip;
        return valueOperation;
    }

    private boolean val() {
        if (getCurrentTokenLexemeInc() == Lexeme.VAR) {
            if (!tableOfVariables.containsKey(getLastTokenValue())) {
                System.err.println("Error: Variety " + getLastTokenValue() + " not initialize");
                System.exit(6);
            }
            tokensRPN.add(getLastTokenValue());
            return true;
        } else {
            position--;
        }
        if (getCurrentTokenLexemeInc() == Lexeme.DIGIT) {
            tokensRPN.add(getLastTokenValue());
            return true;
        } else {
            position--;
        }
        return breakValue();
    }

    private boolean breakValue() {
        boolean breakValue = false;
        int zip = position;

        if (getCurrentTokenLexemeInc() == Lexeme.PARENTHESIS_LEFT) {
            stack.push(getLastTokenValue());
            if (value()) {
                if (getCurrentTokenLexemeInc() == Lexeme.PARENTHESIS_RIGHT) {
                    while (!stack.peek().equals("(")) {
                        tokensRPN.add(stack.pop());
                    }
                    stack.pop();
                    breakValue = true;
                }
            }
        }
        position = breakValue ? position : zip;
        return breakValue;
    }

    private boolean setAssign() {
        return setClear() || setAdd() || setRemove();
    }

    private boolean forModule() {
        boolean forModule = false;
        int zip = position;

        if (getCurrentTokenLexemeInc() == Lexeme.FOR) {
            if (exprFor()) {
                if (body()) {
                    forModule = true;
                    tokensRPN.set(p1, String.valueOf(tokensRPN.size() + 2));//перепрыгиваем p2&!
                    tokensRPN.add(String.valueOf(p2));
                    tokensRPN.add("!");
                }
            }
        }
        position = forModule ? position : zip;
        return forModule;
    }

    private boolean exprFor() {
        boolean exprFor = false;
        int zip = position;

        if (getCurrentTokenLexemeInc() == Lexeme.PARENTHESIS_LEFT) {
            if (assign()) {
                if (logExprFor()) {
                    if (assignOperation()) {
                        if (getCurrentTokenLexemeInc() == Lexeme.PARENTHESIS_RIGHT) {
                            exprFor = true;
                        }
                    }
                }
            }
        }
        position = exprFor ? position : zip;
        return exprFor;
    }

    private boolean logExprFor() {
        boolean logExprFor = false;
        int zip = position;

        p2 = tokensRPN.size();
        if (setContains() || setIsEmpty() || logExpr()) {
            if (getCurrentTokenLexemeInc() == Lexeme.SEMICOLON) {
                logExprFor = true;
                p1 = tokensRPN.size();
                tokensRPN.add("p1");
                tokensRPN.add("!F");
            }
        }
        position = logExprFor ? position : zip;
        return logExprFor;
    }

    private boolean ifModule() {
        boolean ifModule = false;
        int zip = position;

        if (getCurrentTokenLexemeInc() == Lexeme.IF) {
            if (exprIf()) {
                if (body()) {
                    ifModule = true;
                    tokensRPN.set(p1, String.valueOf(tokensRPN.size() + 2));//перепрыгиваем p2&!
                    p2 = tokensRPN.size();
                    tokensRPN.add("p2");
                    tokensRPN.add("!");
                    if (tokens.size() != position && getCurrentTokenLexemeInc() == Lexeme.ELSE) {
                        if (body()) {
                        }
                    }
                    tokensRPN.set(p2, String.valueOf(tokensRPN.size()));
                }
            }
        }
        position = ifModule ? position : zip;
        return ifModule;
    }

    private boolean body() {
        boolean body = false;
        int zip = position;

        if (getCurrentTokenLexemeInc() == Lexeme.BRACE_LEFT) {
            while (init() || assign() || setAssign()) {
            }
            if (getCurrentTokenLexemeInc() == Lexeme.BRACE_RIGHT) {
                body = true;
            }
        }
        position = body ? position : zip;
        return body;
    }

    private boolean exprIf() {
        boolean exprIf = false;
        int zip = position;

        if (getCurrentTokenLexemeInc() == Lexeme.PARENTHESIS_LEFT) {
            if (setContains() || setIsEmpty() || logExpr()) {
                if (getCurrentTokenLexemeInc() == Lexeme.PARENTHESIS_RIGHT) {
                    exprIf = true;
                    //
                    p1 = tokensRPN.size();
                    tokensRPN.add("p1");
                    tokensRPN.add("!F");
                }
            }
        }
        position = exprIf ? position : zip;
        return exprIf;
    }

    private boolean logExpr() {
        boolean logExpr = false;
        int zip = position;
        String op = "";
        ArrayList<String> stackZip = new ArrayList<>();

        if (assignOperation() || value()) {
            if (getCurrentTokenLexemeInc() == Lexeme.LOGIC_OP) {
                op = getLastTokenValue();
                while (!stack.empty()) {
                    String pop = stack.pop();
                    tokensRPN.add(pop);
                    stackZip.add(pop);
                }
                if (assignOperation() || value()) {
                    while (!stack.empty()) {
                        tokensRPN.add(stack.pop());
                    }
                    logExpr = true;
                    tokensRPN.add(op);
                    stackZip.clear();
                }
            }
        }
        if (!logExpr) {
            position = zip;
            if (op.length() != 0) {
                for (int i = stackZip.size() - 1; i >= 0; i--) {
                    stack.push(stackZip.get(i));
                    tokensRPN.remove(tokensRPN.size() - 1);
                }
                stackZip.clear();
            }
        }
        return logExpr;
    }

    private boolean setIsEmpty() {
        boolean setIsEmpty = false;
        int zip = position;
        boolean add = false;
        String op, var;

        if (getCurrentTokenLexemeInc() == Lexeme.VAR) {
            add = tokensRPN.add(getLastTokenValue());
            var = getLastTokenValue();

            if (getCurrentTokenLexemeInc() == Lexeme.ISEMPTY) {
                if (tableOfVariables.get(var)[0].equals("set")) {
                    op = getLastTokenValue();
                    while (!stack.empty()) {
                        tokensRPN.add(stack.pop());
                    }
                    tokensRPN.add(op);
                    setIsEmpty = true;
                } else {
                    System.err.println("Error: Try to add to not Set variable");
                    System.exit(301);
                }
            }
        }
        if (!setIsEmpty) {
            position = zip;
            if (add) {
                tokensRPN.remove(tokensRPN.size() - 1);
            }
        }
        return setIsEmpty;
    }

    private boolean setContains() {
        boolean setContains = false;
        int zip = position;
        boolean add = false;
        String op, var;

        if (getCurrentTokenLexemeInc() == Lexeme.VAR) {
            add = tokensRPN.add(getLastTokenValue());
            var = getLastTokenValue();

            if (getCurrentTokenLexemeInc() == Lexeme.CONTAINS) {
                if (tableOfVariables.get(var)[0].equals("set")) {
                    op = getLastTokenValue();
                    if (value()) {
                        while (!stack.empty()) {
                            tokensRPN.add(stack.pop());
                        }
                        tokensRPN.add(op);
                        setContains = true;

                    }
                } else {
                    System.err.println("Error: Try to contains to not Set variable");
                    System.exit(301);
                }
            }
        }
        if (!setContains) {
            position = zip;
            if (add) {
                tokensRPN.remove(tokensRPN.size() - 1);
            }
        }
        return setContains;
    }

    private boolean setAdd() {
        boolean setAdd = false;
        int zip = position;
        boolean add = false;
        String op, var;

        if (getCurrentTokenLexemeInc() == Lexeme.VAR) {
            add = tokensRPN.add(getLastTokenValue());
            var = getLastTokenValue();

            if (getCurrentTokenLexemeInc() == Lexeme.ADD) {
                if (tableOfVariables.get(var)[0].equals("set")) {
                    op = getLastTokenValue();
                    if (value()) {
                        if (getCurrentTokenLexemeInc() == Lexeme.SEMICOLON) {
                            while (!stack.empty()) {
                                tokensRPN.add(stack.pop());
                            }
                            tokensRPN.add(op);
                            setAdd = true;
                        }
                    }
                } else {
                    System.err.println("Error: Try to add to not Set variable");
                    System.exit(301);
                }
            }
        }
        if (!setAdd) {
            position = zip;
            if (add) {
                tokensRPN.remove(tokensRPN.size() - 1);
            }
        }
        return setAdd;
    }

    private boolean setRemove() {
        boolean setRemove = false;
        int zip = position;
        boolean add = false;
        String op, var;

        if (getCurrentTokenLexemeInc() == Lexeme.VAR) {
            add = tokensRPN.add(getLastTokenValue());
            var = getLastTokenValue();

            if (getCurrentTokenLexemeInc() == Lexeme.REMOVE) {
                if (tableOfVariables.get(var)[0].equals("set")) {
                    op = getLastTokenValue();
                    if (value()) {
                        if (getCurrentTokenLexemeInc() == Lexeme.SEMICOLON) {
                            while (!stack.empty()) {
                                tokensRPN.add(stack.pop());
                            }
                            tokensRPN.add(op);
                            setRemove = true;
                        }
                    }
                } else {
                    System.err.println("Error: Try to remove from not Set variable");
                    System.exit(301);
                }
            }
        }
        if (!setRemove) {
            position = zip;
            if (add) {
                tokensRPN.remove(tokensRPN.size() - 1);
            }
        }
        return setRemove;
    }

    private boolean setClear() {
        boolean setClear = false;
        int zip = position;
        boolean add = false;
        String op, var;

        if (getCurrentTokenLexemeInc() == Lexeme.VAR) {
            add = tokensRPN.add(getLastTokenValue());
            var = getLastTokenValue();

            if (getCurrentTokenLexemeInc() == Lexeme.CLEAR) {
                if (tableOfVariables.get(var)[0].equals("set")) {
                    op = getLastTokenValue();
                    if (getCurrentTokenLexemeInc() == Lexeme.SEMICOLON) {
                        if (!stack.empty()) {
                            System.out.println("WOW STACK is not empty");
                        }
                        while (!stack.empty()) {
                            tokensRPN.add(stack.pop());
                        }
                        tokensRPN.add(op);
                        setClear = true;
                    }
                } else {
                    System.err.println("Error: Try to clear not Set variable");
                    System.exit(300);
                }
            }
        }
        if (!setClear) {
            position = zip;
            if (add) {
                tokensRPN.remove(tokensRPN.size() - 1);
            }
        }
        return setClear;
    }

    private Lexeme getCurrentTokenLexemeInc() {
        try {
            return tokens.get(position++).getLexeme();
        } catch (IndexOutOfBoundsException ex) {
            position--;
            System.err.println("Error: Lexeme \"" + tokens.get(--position).getLexeme() + "\" expected");
            System.exit(3);
        }
        return null;
    }

    private String getLastTokenValue() {
        return tokens.get(position - 1).getValue();
    }

    Object[] valueCreate(String type, Object value) {
        Object[] ret = new Object[2];

        ret[0] = type;
        ret[1] = value;
        return ret;
    }

    String printTOV(Map<String, Object[]> tov) {
        StringBuilder s = new StringBuilder();
        Set<String> keys = tov.keySet();
        String[] keyss = keys.toArray(new String[0]);

        s.append("[");
        for (int i = 0; i < keyss.length - 1; i++) {
            Object[] values = tov.get(keyss[i]);
            String value0 = values[0].toString();
            String value1 = values[1].toString();

            s.append("[");
            s.append(keyss[i]);
            s.append(", [");
            s.append(value0);
            s.append(", ");
            if (value1.equals("")) {
                s.append("null");
            } else {
                s.append(value1);
            }
            s.append("]], ");
        }
        String value0 = tov.get(keyss[keyss.length - 1])[0].toString();
        String value1 = tov.get(keyss[keyss.length - 1])[1].toString();

        s.append("[");
        s.append(keyss[keyss.length - 1]);
        s.append(", [");
        s.append(value0);
        s.append(", ");
        if (value1.equals("")) {
            s.append("null");
        } else {
            s.append(value1);
        }
        s.append("]]");
        s.append("]");
        return s.toString();
    }

    private int getPriority(String str) {
        switch (str) {
            case "+":
                return 1;
            case "*":
                return 2;
            case "-":
                return 1;
            case "/":
                return 2;
            case "(":
                return 0;
            default:
                System.err.println("Error: In symbol " + str);
                System.exit(5);
                return 0;
        }
    }
}