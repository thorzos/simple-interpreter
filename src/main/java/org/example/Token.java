package org.example;


public record Token(TokenType type, String content) {

    public String value() {
        return content;
    }

    @Override
    public String toString() {
        return type + ": " + content;
    }
}
