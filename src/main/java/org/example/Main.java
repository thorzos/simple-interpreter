package org.example;

public class Main {

    public static void main(String[] args) {
        String test = "xyooooodsfisja = \t \0 10";

        Lexer scanner = new Lexer(test);

        var tokens = scanner.getTokens();

        System.out.println(tokens);

    }
}