import java.util.regex.Pattern;

public enum Lexeme {
    TYPE(Pattern.compile("^int|set$")),
    ADD(Pattern.compile("^add$")),
    REMOVE(Pattern.compile("^remove$")),
    CONTAINS(Pattern.compile("^contains$")),
    ISEMPTY(Pattern.compile("^isempty$")),
    CLEAR(Pattern.compile("^clear$")),
    FOR(Pattern.compile("^for$")),
    IF(Pattern.compile("^if$")),
    ELSE(Pattern.compile("^else$")),
    VAR(Pattern.compile("^[a-z]+$")),
    ASSIGN_OP(Pattern.compile("^=$")),//+
    DIGIT(Pattern.compile("^0|[1-9][0-9]*")),
    ARITHMETIC_OP(Pattern.compile("^\\+|-|\\*|/$")),//+
    LOGIC_OP(Pattern.compile("^<|>|<=|>=|!=|==$")),
    INVERSE(Pattern.compile("!")),//+   !F//+
    WS(Pattern.compile("^\\s+$")),
    BRACE_LEFT(Pattern.compile("^\\{$")),
    BRACE_RIGHT(Pattern.compile("^}$")),
    PARENTHESIS_LEFT(Pattern.compile("^\\($")),
    PARENTHESIS_RIGHT(Pattern.compile("^\\)$")),
    SEMICOLON(Pattern.compile("^;$"));

    Pattern pattern;

    Lexeme(Pattern pattern) {
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return pattern;
    }
}
