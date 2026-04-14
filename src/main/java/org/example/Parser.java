package org.example;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int index = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            stmts.add(parseStatement());
        }
        return stmts;
    }

    private Token peek() {
        return tokens.get(index);
    }

    private Token advance() {
        return tokens.get(index++);
    }

    private boolean check(TokenType type) {
        return peek().type() == type;
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private Token expect(TokenType type) {
        if (check(type)) return advance();
        throw new RuntimeException("Expected " + type + " but got " + peek().type());
    }

    private Stmt parseStatement() {
        if (check(TokenType.IF)) return parseIf();
        if (check(TokenType.WHILE)) return parseWhile();
        if (check(TokenType.FUN)) return parseFun();
        if (check(TokenType.RETURN)) return parseReturn();
        return parseAssign();
    }

    private Stmt parseIf() {
        expect(TokenType.IF);
        Expr condition = parseExpression();
        expect(TokenType.THEN);
        Stmt then = parseStatement();

        Stmt _else = null;
        if (match(TokenType.ELSE)) {
            _else = parseStatement();
        }

        return new Stmt.If(condition, then, _else);
    }

    private Stmt parseWhile() {
        expect(TokenType.WHILE);
        Expr condition = parseExpression();
        expect(TokenType.DO);
        Stmt _do = parseSequence();

        return new Stmt.While(condition, _do);
    }

    private Stmt parseSequence() {
        Stmt first = parseStatement();
        if (!check(TokenType.COMMA)) return first;

        List<Stmt> stmts = new ArrayList<>();
        stmts.add(first);
        while (match(TokenType.COMMA)) {
            stmts.add(parseStatement());
        }

        return new Stmt.Block(stmts);
    }

    private Stmt parseFun() {
        expect(TokenType.FUN);
        String name = expect(TokenType.IDENTIFIER).value();

        expect(TokenType.LEFT_PARENTHESES);
        List<String> params = new ArrayList<>();
        if (!check(TokenType.RIGHT_PARENTHESES)) {
            do {
                params.add(expect(TokenType.IDENTIFIER).value());
            } while (match(TokenType.COMMA));
        }
        expect(TokenType.RIGHT_PARENTHESES);

        Stmt body = parseBlock();
        return new Stmt.FunDecl(name, params, body);
    }

    private Stmt parseBlock() {
        expect(TokenType.LEFT_BRACES);
        ArrayList<Stmt> stmts = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACES) && index < tokens.size()) {
            stmts.add(parseStatement());
            match(TokenType.COMMA);
        }

        expect(TokenType.RIGHT_BRACES);
        return new Stmt.Block(stmts);
    }

    private Stmt parseReturn() {
        expect(TokenType.RETURN);
        Expr value = parseExpression();
        return new Stmt.Return(value);
    }

    private Stmt parseAssign() {
        String name = expect(TokenType.IDENTIFIER).value();
        expect(TokenType.ASSIGN);
        Expr value = parseExpression();
        return new Stmt.Assign(name, value);
    }

    private Expr parseExpression() {
        Expr left = parseAddition();

        while (check(TokenType.EQUAL) ||
                check(TokenType.LESS) || check(TokenType.LESS_EQUAL) ||
                check(TokenType.GREATER) || check(TokenType.GREATER_EQUAL))
        {
            TokenType op = advance().type();
            Expr right = parseAddition();
            left = new Expr.Binary(left, op, right);
        }

        return left;
    }

    private Expr parseAddition() {
        Expr left = parseMultiply();

        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            TokenType op = advance().type();
            Expr right = parseMultiply();
            left = new Expr.Binary(left, op, right);
        }

        return left;
    }

    private Expr parseMultiply() {
        Expr left = parsePrimary();

        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE)) {
            TokenType op = advance().type();
            Expr right = parsePrimary();
            left = new Expr.Binary(left, op, right);
        }

        return left;
    }

    private Expr parsePrimary() {
        if (check(TokenType.NUMBER)) {
            int value = Integer.parseInt(advance().value());
            return new Expr.Literal(value);
        }

        if (match(TokenType.TRUE)) return new Expr.Literal(1);
        if (match(TokenType.FALSE)) return new Expr.Literal(0);

        if (match(TokenType.LEFT_PARENTHESES)) {
            Expr expr = parseExpression();
            expect(TokenType.RIGHT_PARENTHESES);
            return expr;
        }

        if (check(TokenType.IDENTIFIER)) {
            String name = advance().value();

            if (match(TokenType.LEFT_PARENTHESES)) {
                List<Expr> args = new ArrayList<>();
                if (!check(TokenType.RIGHT_PARENTHESES)) {
                    do {
                        args.add(parseExpression());
                    } while (match(TokenType.COMMA));
                }
                expect(TokenType.RIGHT_PARENTHESES);
                return new Expr.Call(name, args);
            }

            return new Expr.Variable(name);
        }

        throw new RuntimeException("Unexpected token: " + peek().type());
    }

}
