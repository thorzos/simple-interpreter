package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String programText;
    private int index = 0;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "if", TokenType.IF,
            "then", TokenType.THEN,
            "else", TokenType.ELSE,
            "while", TokenType.WHILE,
            "do", TokenType.DO,
            "return", TokenType.RETURN,
            "fun", TokenType.FUN,
            "true", TokenType.TRUE,
            "false", TokenType.FALSE
    );

    public Lexer(String programText) {
        this.programText = programText;
    }

    private char moveNext() {
        return programText.charAt(index++);
    }

    public List<Token> getTokens() {
        while (index < programText.length()) {
            char current = moveNext();

            switch (current) {
                case ' ', '\t', '\r', '\n', '\0' -> {}

                case '(' -> tokens.add(new Token(TokenType.LEFT_PARENTHESES, "("));
                case ')' -> tokens.add(new Token(TokenType.RIGHT_PARENTHESES, ")"));
                case '{' -> tokens.add(new Token(TokenType.LEFT_BRACES, "{"));
                case '}' -> tokens.add(new Token(TokenType.RIGHT_BRACES, "}"));
                case '+' -> tokens.add(new Token(TokenType.PLUS, "+"));
                case '-' -> tokens.add(new Token(TokenType.MINUS, "-"));
                case '*' -> tokens.add(new Token(TokenType.MULTIPLY, "*"));
                case '/' -> tokens.add(new Token(TokenType.DIVIDE, "/"));
                case ',' -> tokens.add(new Token(TokenType.COMMA, ","));

                case '=' -> {
                    if (lookAhead() == '=') { index++; tokens.add(new Token(TokenType.EQUAL, "==")); }
                    else tokens.add(new Token(TokenType.ASSIGN, "="));
                }
                case '>' -> {
                    if (lookAhead() == '=') { index++; tokens.add(new Token(TokenType.GREATER_EQUAL, ">=")); }
                    else tokens.add(new Token(TokenType.GREATER, ">"));
                }
                case '<' -> {
                    if (lookAhead() == '=') { index++; tokens.add(new Token(TokenType.LESS_EQUAL, "<=")); }
                    else tokens.add(new Token(TokenType.LESS, "<"));
                }

                default -> {
                    if (Character.isLetter(current)) {
                        scanIdentifier(current);
                    } else if (Character.isDigit(current)) {
                        scanNumber(current);
                    } else {
                        throw new RuntimeException("Unexpected character: " + current);
                    }
                }
            }
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private char lookAhead() {
        return index < programText.length() ? programText.charAt(index) : '\0';
    }

    private void scanIdentifier(char current) {
        StringBuilder identifier = new StringBuilder();
        identifier.append(current);

        while (index < programText.length() && (Character.isLetterOrDigit(programText.charAt(index)) || programText.charAt(index) == '_')) {
            identifier.append(moveNext());
        }
        String txt = identifier.toString();
        tokens.add(new Token(KEYWORDS.getOrDefault(txt, TokenType.IDENTIFIER), txt));
    }

    private void scanNumber(char current) {
        StringBuilder number = new StringBuilder();
        number.append(current);

        while (index < programText.length() && Character.isDigit(programText.charAt(index))) {
            number.append(moveNext());
        }

        tokens.add(new Token(TokenType.NUMBER, number.toString()));
    }

}