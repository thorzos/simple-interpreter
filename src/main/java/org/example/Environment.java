package org.example;


import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Map<String, Integer> variables = new HashMap<>();
    private final Map<String, Stmt.FunDecl> functions = new HashMap<>();

    private final Environment parent;

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public Environment() {
        this.parent = null;
    }

    public Map<String, Integer> getLocalVars() {
        return variables;
    }

    public int getVar(String name) {
        if (variables.containsKey(name)) return variables.get(name);
        if (parent != null) return parent.getVar(name);
        throw new RuntimeException("Undefined variable: " + name);
    }

    public void setVar(String name, int value) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
            return;
        }
        if (parent != null && parent.hasVar(name)) {
            parent.setVar(name, value);
            return;
        }
        variables.put(name, value);
    }

    public boolean hasVar(String name) {
        return variables.containsKey(name) || (parent != null && parent.hasVar(name));
    }

    public Stmt.FunDecl getFun(String name) {
        if (functions.containsKey(name)) return functions.get(name);
        if (parent != null) return parent.getFun(name);
        throw new RuntimeException("Undefined Fun: " + name);
    }

    public void defineFun(Stmt.FunDecl decl) {
        functions.put(decl.name(), decl);
    }

}
