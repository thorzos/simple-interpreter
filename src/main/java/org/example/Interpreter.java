package org.example;

import java.util.List;
import java.util.Map;

public class Interpreter {

    private final Environment globalVars = new Environment();

    public Map<String, Integer> interpret(List<Stmt> stmtList) {
        try {
            stmtList.forEach(stmt -> apply(stmt, globalVars));
        } catch (ReturnInterrupt e) {
            throw new RuntimeException("'return' was used outside function");
        }
        return globalVars.getLocalVars();
    }

    private void apply(Stmt stmt, Environment env) {
        switch (stmt) {
            case Stmt.Assign a  -> env.setVar(a.name(), evaluate(a.value(), env));
            case Stmt.If i      -> applyIf(i, env);
            case Stmt.While w   -> applyWhile(w, env);
            case Stmt.Block b   -> b.stmts().forEach(s-> apply(s, env));
            case Stmt.Return r  -> throw new ReturnInterrupt(evaluate(r.value(), env));
            case Stmt.FunDecl f -> globalVars.defineFun(f);
        }
    }

    private int evaluate(Expr expr, Environment env) {
        return switch (expr) {
            case Expr.Literal l     -> l.value();
            case Expr.Variable v    -> env.getVar(v.name());
            case Expr.Binary b      -> evaluateBinary(b, env);
            case Expr.Call c        -> evaluateCall(c, env);
        };
    }

    private void applyIf(Stmt.If stmt, Environment env) {
        if (evaluate(stmt.condition(), env) != 0) {
            apply(stmt.then(), env);
        } else if (stmt.else_() != null) { // spec does not specify
            apply(stmt.else_(), env);
        }
    }

    private void applyWhile(Stmt.While stmt, Environment env) {
        while (evaluate(stmt.condition(), env) != 0) {
            apply(stmt.body(), env);
        }
    }
    
    private int evaluateBinary(Expr.Binary bin, Environment env) {
        int left = evaluate(bin.left(), env);
        int right = evaluate(bin.right(), env);

        return switch (bin.op()) {
            case PLUS       -> left + right;
            case MINUS      -> left - right;
            case MULTIPLY   -> left * right;
            case DIVIDE     -> {
                if (right == 0) throw new RuntimeException("Division by zero");
                yield left / right;
            }
            case EQUAL      -> left == right ? 1 : 0;
            case GREATER    -> left > right ? 1 : 0;
            case GREATER_EQUAL -> left >= right ? 1 : 0;
            case LESS       ->  left < right ? 1 : 0;
            case LESS_EQUAL -> left <= right ? 1 : 0;

            default -> throw new RuntimeException("Binary operator not supported: " + bin.op());
        };
    }
    
    private int evaluateCall(Expr.Call expr, Environment env) {
        Stmt.FunDecl fun = globalVars.getFun(expr.name());

        Environment funEnv = new Environment(globalVars);

        if (expr.args().size() != fun.params().size()) {
            throw new RuntimeException(
                    "Function " + expr.name() +
                    " expects " + fun.params().size() +
                    " arguments but got " + expr.args().size());
        }

        for (int i = 0; i < fun.params().size(); i++) {
            funEnv.setVar(fun.params().get(i), evaluate(expr.args().get(i), env));
        }

        try {
            apply(fun.body(), funEnv);
            return 0;
        } catch (ReturnInterrupt returned) {
            return returned.getValue();
        }
    }
}