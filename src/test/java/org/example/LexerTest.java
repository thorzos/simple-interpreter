package org.example;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class LexerTest {

    private void assertToken(Token token, TokenType expectedType, String expectedValue) {
        assertEquals(expectedType, token.type());
        assertEquals(expectedValue, token.value());
    }

    private List<Token> scan(String source) {
        return new Lexer(source).getTokens();
    }

    @Test
    public void validInput() {
        var tokens = scan("thenif then return false == = \t \0 10");

        List<Token> expectedTokens = new ArrayList<>(List.of(
                new Token(TokenType.IDENTIFIER, "thenif"),
                new Token(TokenType.THEN,       "then"),
                new Token(TokenType.RETURN,     "return"),
                new Token(TokenType.FALSE,      "false"),
                new Token(TokenType.EQUAL,      "=="),
                new Token(TokenType.ASSIGN,     "="),
                new Token(TokenType.NUMBER,     "10"),
                new Token(TokenType.EOF,        "")
        ));

        assertEquals(expectedTokens, tokens);
    }

    @Test
    void singleNumber() {
        var tokens = scan("67");
        assertToken(tokens.get(0), TokenType.NUMBER, "67");
        assertToken(tokens.get(1), TokenType.EOF, "");
    }

    @Test
    void singleIdentifier() {
        var tokens = scan("test");
        assertToken(tokens.getFirst(), TokenType.IDENTIFIER, "test");
    }

    @Test
    void identifierWithDigitsAndUnderscore() {
        var tokens = scan("jon123_snow");
        assertToken(tokens.getFirst(), TokenType.IDENTIFIER, "jon123_snow");
    }

    @Test
    void arithmeticOperators() {
        var tokens = scan("+ - * /");
        assertToken(tokens.get(0), TokenType.PLUS,     "+");
        assertToken(tokens.get(1), TokenType.MINUS,    "-");
        assertToken(tokens.get(2), TokenType.MULTIPLY, "*");
        assertToken(tokens.get(3), TokenType.DIVIDE,   "/");
    }

    @Test
    void assignVsEqual() {
        var tokens = scan("= ==");
        assertToken(tokens.get(0), TokenType.ASSIGN, "=");
        assertToken(tokens.get(1), TokenType.EQUAL,  "==");
    }

    @Test
    void comparisonOperators() {
        var tokens = scan("< <= > >=");
        assertToken(tokens.get(0), TokenType.LESS,          "<");
        assertToken(tokens.get(1), TokenType.LESS_EQUAL,    "<=");
        assertToken(tokens.get(2), TokenType.GREATER,       ">");
        assertToken(tokens.get(3), TokenType.GREATER_EQUAL, ">=");
    }

    @Test
    void keywords() {
        var tokens = scan("if then else while do return fun true false");
        assertToken(tokens.get(0), TokenType.IF,     "if");
        assertToken(tokens.get(1), TokenType.THEN,   "then");
        assertToken(tokens.get(2), TokenType.ELSE,   "else");
        assertToken(tokens.get(3), TokenType.WHILE,  "while");
        assertToken(tokens.get(4), TokenType.DO,     "do");
        assertToken(tokens.get(5), TokenType.RETURN, "return");
        assertToken(tokens.get(6), TokenType.FUN,    "fun");
        assertToken(tokens.get(7), TokenType.TRUE,   "true");
        assertToken(tokens.get(8), TokenType.FALSE,  "false");;
    }

    @Test
    void keywordsAreNotIdentifiers() {
        var tokens = scan("if");
        assertToken(tokens.getFirst(), TokenType.IF, "if");
        assertNotEquals(TokenType.IDENTIFIER, tokens.getFirst().type());
    }

    @Test
    void identifierStartingWithKeyword() {
        var tokens = scan("iffy");
        assertToken(tokens.getFirst(), TokenType.IDENTIFIER, "iffy");
    }

    @Test
    void parenthesesAndBraces() {
        var tokens = scan("( ) { }");
        assertToken(tokens.get(0), TokenType.LEFT_PARENTHESES,  "(");
        assertToken(tokens.get(1), TokenType.RIGHT_PARENTHESES, ")");
        assertToken(tokens.get(2), TokenType.LEFT_BRACES,        "{");
        assertToken(tokens.get(3), TokenType.RIGHT_BRACES,       "}");
    }

    @Test
    void comma() {
        var tokens = scan(",");
        assertToken(tokens.getFirst(), TokenType.COMMA, ",");
    }

    @Test
    void whitespaceIsIgnored() {
        var tokens = scan("  x   =   2  ");
        assertEquals(3, tokens.stream()
                .filter(t -> t.type() != TokenType.EOF)
                .count());
    }

    @Test
    void newlinesAreIgnored() {
        var tokens = scan("x\n=\n2");
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "x");
        assertToken(tokens.get(1), TokenType.ASSIGN,     "=");
        assertToken(tokens.get(2), TokenType.NUMBER,     "2");
    }

    @Test
    void eofIsLastToken() {
        var tokens = scan("x");
        assertToken(tokens.getLast(), TokenType.EOF, "");
    }

    @Test
    void emptyInputIsJustEof() {
        var tokens = scan("");
        assertEquals(1, tokens.size());
        assertToken(tokens.getFirst(), TokenType.EOF, "");
    }

    @Test
    void fullExpressionTokenized() {
        var tokens = scan("x = (x + 2) * 2");
        assertToken(tokens.get(0), TokenType.IDENTIFIER,        "x");
        assertToken(tokens.get(1), TokenType.ASSIGN,            "=");
        assertToken(tokens.get(2), TokenType.LEFT_PARENTHESES,  "(");
        assertToken(tokens.get(3), TokenType.IDENTIFIER,        "x");
        assertToken(tokens.get(4), TokenType.PLUS,              "+");
        assertToken(tokens.get(5), TokenType.NUMBER,            "2");
        assertToken(tokens.get(6), TokenType.RIGHT_PARENTHESES, ")");
        assertToken(tokens.get(7), TokenType.MULTIPLY,          "*");
        assertToken(tokens.get(8), TokenType.NUMBER,            "2");
        assertToken(tokens.getLast(), TokenType.EOF,            "");
    }
}