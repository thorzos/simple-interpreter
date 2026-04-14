package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        StringBuilder sb = new StringBuilder();
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            sb.append(currentLine).append('\n');
        }
        try {
            Lexer lexer = new Lexer(sb.toString());
            Parser parser = new Parser(lexer.getTokens());
            var interpretedVars = new Interpreter().interpret(parser.parse());

            interpretedVars.forEach((var, val) -> System.out.println(var + ": " + val));

        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (StackOverflowError error) {
            System.err.println("Stack Overflow error: " + error.getMessage());
        }
    }
}