public class Main {
    public static void main(String[] args) {
        final String string = "set a; if (a isempty) {int b; b=5;} else {int c; c = 3;}";
        Lexer lexer = new Lexer();
        Parser parser = new Parser();
        StackMachine stackMachine = new StackMachine();

        System.out.println("[" + string + "]");
        System.out.println(parser.lang(lexer.recognize(string)));
        System.out.println(parser.printTOV(stackMachine.stackMachine(parser)));
    }
}
