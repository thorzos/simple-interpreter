package org.example;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class InterpreterTest {

    private Map<String, Integer> runCode(String source) {
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.getTokens());
        return new Interpreter().interpret(parser.parse());
    }

    @Test
    public void test_Unary() {
        String input =
                """
                x = -10
                """;

        Map<String, Integer> result = runCode(input);

        assertEquals(1, result.size());
        assertEquals(-10, result.get("x"));
    }

    @Test
    public void test_NumberBiggerThanInteger_throwsRuntimeException() {
        String input =
                """
                x = %d
                """.formatted(Long.MAX_VALUE);

        assertThrows(RuntimeException.class, () -> runCode(input));
    }

    @Test
    public void testExample1_BasicMath() {
        String input =
                """
                x = 2
                y = (x + 2) * 2
                """;
        Map<String, Integer> result = runCode(input);

        assertEquals(2, result.size());
        assertEquals(2, result.get("x"));
        assertEquals(8, result.get("y"));
    }

    @Test
    public void testExample2_IfElse() {
        String input =
                """
                x = 20
                if x > 10 then y = 100 else y = 0
                """;
        Map<String, Integer> result = runCode(input);

        assertEquals(2, result.size());
        assertEquals(20, result.get("x"));
        assertEquals(100, result.get("y"));
    }

    @Test
    public void testExample3_WhileAndCommaSequence() {
        String input =
                """
                x = 0
                y = 0
                while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
                """;
        Map<String, Integer> result = runCode(input);

        assertEquals(2, result.size());
        assertEquals(3, result.get("x"));
        assertEquals(11, result.get("y"));
    }

    @Test
    public void testExample4_FunctionDeclaration() {
        String input =
                """
                fun add(a, b) { return a + b }
                four = add( 2, 2)
                """;
        Map<String, Integer> result = runCode(input);

        assertEquals(1, result.size(), "Should only print global variables, not functions");
        assertEquals(4, result.get("four"));
    }

    @Test
    public void testExample5_RecursiveFunction() {
        String input =
                """
                fun fact_rec(n) { if n <= 0 then return 1 else return n*fact_rec(n-1) }
                a = fact_rec(5)
                """;
        Map<String, Integer> result = runCode(input);

        assertEquals(1, result.size());
        assertEquals(120, result.get("a"));
    }

    @Test
    public void testExample6_IterativeFunction() {
        String input =
                """
                fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
                b = fact_iter(5)
                """;
        Map<String, Integer> result = runCode(input);

        assertEquals(1, result.size());
        assertEquals(120, result.get("b"));
    }

}
