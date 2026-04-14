package org.example;

import java.util.List;

public sealed interface Expr permits Expr.Literal, Expr.Binary, Expr.Unary, Expr.Variable, Expr.Call {

    record Literal(int value) implements Expr {}

    record Binary(Expr left, TokenType op, Expr right) implements Expr {}

    record Unary(TokenType op, Expr operand) implements Expr {}

    record Variable(String name) implements Expr {}

    record Call(String name, List<Expr> args) implements Expr {}
}