
package tac;

import java.util.ArrayList;
import java.util.List;

public class TACGenerator {

    // Expression interface
    interface Expr {
    }

    // Variable expression, e.g. a
    static class Variable implements Expr {
        String name;

        Variable(String name) {
            this.name = name;
        }
    }

    // Literal expression, e.g. 10 or 3.5
    static class Literal implements Expr {
        String value;

        Literal(String value) {
            this.value = value;
        }
    }

    // Binary expression, e.g. a + b
    static class Binary implements Expr {
        Expr left;
        String operator;
        Expr right;

        Binary(Expr left, String operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    private final List<String> instructions = new ArrayList<>();
    private int tempCount = 0;

    // Generate a new temporary variable
    private String newTemp() {
        tempCount++;
        return "t" + tempCount;
    }

    // Convert an expression into TAC
    public String generateExpression(Expr expr) {

        if (expr instanceof Variable) {
            return ((Variable) expr).name;
        }

        if (expr instanceof Literal) {
            return ((Literal) expr).value;
        }

        if (expr instanceof Binary) {
            Binary binary = (Binary) expr;

            // Generate left and right expressions first
            String left = generateExpression(binary.left);
            String right = generateExpression(binary.right);

            String temp = newTemp();

            instructions.add(
                temp + " = " + left + " "
                + binary.operator + " " + right
            );

            return temp;
        }

        throw new IllegalArgumentException(
            "Unsupported expression type"
        );
    }

    // Generate TAC for an assignment
    public void generateAssignment(String variable, Expr expr) {
        String result = generateExpression(expr);
        instructions.add(variable + " = " + result);
    }

    // Generate TAC for print
    public void generatePrint(Expr expr) {
        String result = generateExpression(expr);
        instructions.add("print " + result);
    }

    // Display generated instructions
    public void printTAC() {
        System.out.println("\n--- Three Address Code ---");

        for (String instruction : instructions) {
            System.out.println(instruction);
        }
    }

    // Simple standalone test
    public static void main(String[] args) {

        TACGenerator generator = new TACGenerator();

        System.out.println("=== TAC Generator Test ===");

        // Test 1: a = b + c * d
        Expr multiplication = new Binary(
            new Variable("c"),
            "*",
            new Variable("d")
        );

        Expr addition = new Binary(
            new Variable("b"),
            "+",
            multiplication
        );

        generator.generateAssignment("a", addition);

        // Test 2: x = a - 10
        Expr subtraction = new Binary(
            new Variable("a"),
            "-",
            new Literal("10")
        );

        generator.generateAssignment("x", subtraction);

        // Test 3: print x
        generator.generatePrint(new Variable("x"));

        generator.printTAC();

        System.out.println("\n=== Test Finished ===");
    }
}
