class Token {
    private Lexeme lexeme;
    private String value;

    Token(Lexeme lexeme, String value) {
        this.lexeme = lexeme;
        this.value = value;
    }

    Lexeme getLexeme() {
        return lexeme;
    }

    String getValue() {
        return value;
    }

    @Override
    public String toString() {
        System.out.println(getLexeme() + ": " + getValue());
        ;
        return getLexeme().toString();
    }
}
