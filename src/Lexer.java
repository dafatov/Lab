import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

class Lexer {
    private String accumulator = "";
    private int position = 0;
    private Lexeme currentLexeme = null;

    List<Token> recognize(String str) {
        List<Token> tokens = new ArrayList<>();

        if (str.length() != 0) {
            while (position < str.length()) {
                char nextChar = str.charAt(position++);

                accumulator += nextChar;
                if (!hasOneLexeme()) {
                    if (hasAnyLexeme(String.valueOf(nextChar))) {
                        tokens.add(new Token(currentLexeme, back(accumulator)));
                        accumulator = "";
                        position--;
                    } else {
                        System.err.println("Error: Invalid input in position: " + position + "; char: \'" + str.toCharArray()[position - 1] + "\'");
                        System.exit(1);
                    }
                }
            }
            tokens.add(new Token(currentLexeme, accumulator));
        } else {
            System.err.println("Error: Null input");
            System.exit(2);
        }
        return tokens;
    }

    private boolean hasAnyLexeme(String str) {
        for (Lexeme lexeme : Lexeme.values()) {
            Matcher matcher = lexeme.getPattern().matcher(str);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOneLexeme() {
        for (Lexeme lexeme : Lexeme.values()) {
            Matcher matcher = lexeme.getPattern().matcher(accumulator);
            if (matcher.matches()) {
                currentLexeme = lexeme;
                return true;
            }
        }
        return false;
    }

    private String back(String str) {
        return str.substring(0, str.length() - 1);
    }
}