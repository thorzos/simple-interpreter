package org.example;

import java.util.List;

public sealed interface Stmt permits Stmt.Assign, Stmt.If, Stmt.While, Stmt.Block, Stmt.Return, Stmt.FunDecl {

    record Assign(String name, Expr value) implements Stmt {}

    record If(Expr condition, Stmt then, Stmt else_) implements Stmt {}

    record While(Expr condition, Stmt body) implements Stmt {}

    record Block(List<Stmt> stmts) implements Stmt {}

    record Return(Expr value) implements Stmt {}

    record FunDecl(String name, List<String> params, Stmt body) implements Stmt {}

}