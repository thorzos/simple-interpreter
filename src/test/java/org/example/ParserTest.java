package org.example;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ParserTest {

    private List<Token> scan(String programText) {
        return new Lexer(programText).getTokens();
    }

    private List<Stmt> parse(String programText) {
        return new Parser(scan(programText)).parse();
    }

    @Test
    void simpleAssign() {
        var stmts = parse("x = 2");
        assertEquals(1, stmts.size());
        assertInstanceOf(Stmt.Assign.class, stmts.getFirst());

        var assign = (Stmt.Assign) stmts.getFirst();
        assertEquals("x", assign.name());
        assertInstanceOf(Expr.Literal.class, assign.value());
        assertEquals(2, ((Expr.Literal) assign.value()).value());
    }

    @Test
    void assignVariable() {
        var stmts = parse("y = x");
        var assign = (Stmt.Assign) stmts.getFirst();
        assertInstanceOf(Expr.Variable.class, assign.value());
        assertEquals("x", ((Expr.Variable) assign.value()).name());
    }

    @Test
    void additionLeftAssociative() {
        var stmts = parse("x = 1+2+3");
        var expr = ((Stmt.Assign) stmts.getFirst()).value();
        assertInstanceOf(Expr.Binary.class, expr);

        var outer = (Expr.Binary) expr;
        assertEquals(TokenType.PLUS, outer.op());
        assertInstanceOf(Expr.Binary.class, outer.left());  // 1 + 2
        assertInstanceOf(Expr.Literal.class, outer.right());
    }

    @Test
    void multiplyBindsTighterThanAdd() {
        var stmts = parse("x = 1 + 2 * 3");
        var expr = (Expr.Binary) ((Stmt.Assign) stmts.getFirst()).value();

        assertEquals(TokenType.PLUS, expr.op());
        assertInstanceOf(Expr.Literal.class, expr.left());
        assertInstanceOf(Expr.Binary.class, expr.right());  // 2 * 3

        var right = (Expr.Binary) expr.right();
        assertEquals(TokenType.MULTIPLY, right.op());
    }

    @Test
    void parenthesesOverridePrecedence() {
        var stmts = parse("x = (1 + 2) * 3");
        var expr = (Expr.Binary) ((Stmt.Assign) stmts.getFirst()).value();

        assertEquals(TokenType.MULTIPLY, expr.op());
        assertInstanceOf(Expr.Binary.class, expr.left());   // (1 + 2)
    }

    @Test
    void trueAndFalseLiterals() {
        var trueStmts = parse("x = true");
        var falseStmts = parse("x = false");

        var trueExpr = (Expr.Literal) ((Stmt.Assign) trueStmts.getFirst()).value();
        var falseExpr = (Expr.Literal) ((Stmt.Assign) falseStmts.getFirst()).value();

        assertEquals(1, trueExpr.value());
        assertEquals(0, falseExpr.value());
    }

    @Test
    void ifWithElse() {
        var stmts = parse("if x > 10 then y = 100 else y = 0");
        assertInstanceOf(Stmt.If.class, stmts.getFirst());

        var if_ = (Stmt.If) stmts.getFirst();
        assertInstanceOf(Expr.Binary.class, if_.condition());
        assertInstanceOf(Stmt.Assign.class, if_.then());
        assertNotNull(if_.else_());
        assertInstanceOf(Stmt.Assign.class, if_.else_());
    }

    @Test
    void ifWithoutElse() {
        var stmts = parse("if x > 0 then y = 1");
        var if_ = (Stmt.If) stmts.getFirst();
        assertNull(if_.else_());
    }

    @Test
    void nestedIf() {
        var stmts = parse("if x == 1 then if y == 2 then z = 3 else z = 4");
        var outer = (Stmt.If) stmts.getFirst();
        assertInstanceOf(Stmt.If.class, outer.then());
    }

    @Test
    void whileLoop() {
        var stmts = parse("while x < 3 do x = x + 1");
        assertInstanceOf(Stmt.While.class, stmts.getFirst());

        var while_ = (Stmt.While) stmts.getFirst();
        assertInstanceOf(Expr.Binary.class, while_.condition());
        assertInstanceOf(Stmt.Assign.class, while_.body());
    }

    @Test
    void whileTrue() {
        var stmts = parse("while true do x = 1");
        var while_ = (Stmt.While) stmts.getFirst();
        var cond = (Expr.Literal) while_.condition();
        assertEquals(1, cond.value()); // true == 1
    }

    @Test
    void funDeclarationNoParams() {
        var stmts = parse("fun greet() { return 0 }");
        assertInstanceOf(Stmt.FunDecl.class, stmts.getFirst());

        var fun = (Stmt.FunDecl) stmts.getFirst();
        assertEquals("greet", fun.name());
        assertTrue(fun.params().isEmpty());
    }

    @Test
    void funDeclarationWithParams() {
        var stmts = parse("fun add(a, b) { return a + b }");
        var fun = (Stmt.FunDecl) stmts.getFirst();
        assertEquals("add", fun.name());
        assertEquals(List.of("a", "b"), fun.params());
    }

    @Test
    void funBodyIsBlock() {
        var stmts = parse("fun add(a, b) { return a + b }");
        var fun = (Stmt.FunDecl) stmts.getFirst();
        assertInstanceOf(Stmt.Block.class, fun.body());
    }

    @Test
    void returnStatement() {
        var stmts = parse("fun f(n) { return n * 2 }");
        var fun = (Stmt.FunDecl) stmts.getFirst();
        var block = (Stmt.Block) fun.body();
        assertInstanceOf(Stmt.Return.class, block.stmts().getFirst());
    }

    @Test
    void functionCallNoArgs() {
        var stmts = parse("x = foo()");
        var assign = (Stmt.Assign) stmts.getFirst();
        assertInstanceOf(Expr.Call.class, assign.value());

        var call = (Expr.Call) assign.value();
        assertEquals("foo", call.name());
        assertTrue(call.args().isEmpty());
    }

    @Test
    void functionCallWithArgs() {
        var stmts = parse("x = add(1, 2)");
        var call = (Expr.Call) ((Stmt.Assign) stmts.getFirst()).value();
        assertEquals("add", call.name());
        assertEquals(2, call.args().size());
    }

    @Test
    void functionCallNestedExprArgs() {
        var stmts = parse("x = add(a + 1, b * 2)");
        var call = (Expr.Call) ((Stmt.Assign) stmts.getFirst()).value();
        assertInstanceOf(Expr.Binary.class, call.args().get(0));
        assertInstanceOf(Expr.Binary.class, call.args().get(1));
    }

    @Test
    void multipleTopLevelStatements() {
        var stmts = parse("x = 2\ny = 3");
        assertEquals(2, stmts.size());
    }

    @Test
    void sampleTestProgram() {
        var stmts = parse("x = 2\ny = (x + 2) * 2");
        assertEquals(2, stmts.size());
        assertInstanceOf(Stmt.Assign.class, stmts.get(0));
        assertInstanceOf(Stmt.Assign.class, stmts.get(1));

        var yAssign = (Stmt.Assign) stmts.get(1);
        var expr = (Expr.Binary) yAssign.value();
        assertEquals(TokenType.MULTIPLY, expr.op());
    }

    @Test
    void sampleTestProgram2() {
        var stmts = parse("x = 20\nif x > 10 then y = 100 else y = 0");
        assertEquals(2, stmts.size());
        assertInstanceOf(Stmt.If.class, stmts.get(1));
    }

    @Test
    void sampleTestProgram3() {
        var stmts = parse("x = 0 \n y = 0 \n while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1");
        System.out.println(stmts);
        assertEquals(3, stmts.size());
        assertInstanceOf(Stmt.While.class, stmts.get(2));
    }

    @Test
    void sampleTestProgram4_funAdd() {
        var stmts = parse("fun add(a, b) { return a + b }\nfour = add(2, 2)");
        assertEquals(2, stmts.size());
        assertInstanceOf(Stmt.FunDecl.class, stmts.get(0));
        assertInstanceOf(Stmt.Assign.class, stmts.get(1));

        var assign = (Stmt.Assign) stmts.get(1);
        assertInstanceOf(Expr.Call.class, assign.value());
    }

    @Test
    void sampleTestProgram5_factRec() {
        var src = "fun fact_rec(n) { if n <= 0 then return 1 else return n*fact_rec(n-1) }";
        var stmts = parse(src);
        assertInstanceOf(Stmt.FunDecl.class, stmts.getFirst());

        var fun = (Stmt.FunDecl) stmts.getFirst();
        assertEquals("fact_rec", fun.name());
        assertEquals(List.of("n"), fun.params());

        var body = (Stmt.Block) fun.body();
        assertInstanceOf(Stmt.If.class, body.stmts().getFirst());
    }
}
