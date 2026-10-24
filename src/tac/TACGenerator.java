package tac;

import java.util.ArrayList;
import java.util.List;

public class TACGenerator {

    interface Expr {
    }

    static class Variable implements Expr {
        String name;

        Variable(String name) {
            this.name = name;
        }
    }

    static class Literal implements Expr {
        String value;

        Literal(String value) {
            this.value = value;
        }
    }

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
    private int labelCount = 0;

    private String newTemp() {
        tempCount++;
        return "t" + tempCount;
    }

    private String newLabel() {
        labelCount++;
        return "L" + labelCount;
    }

    public String generateExpression(Expr expr) {

        if (expr instanceof Variable) {
            return ((Variable) expr).name;
        }

        if (expr instanceof Literal) {
            return ((Literal) expr).value;
        }

        if (expr instanceof Binary) {

            Binary binary = (Binary) expr;

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

    public void generateAssignment(String variable, Expr expr) {

        String result = generateExpression(expr);

        instructions.add(
            variable + " = " + result
        );
    }

    public void generatePrint(Expr expr) {

        String result = generateExpression(expr);

        instructions.add(
            "print " + result
        );
    }

    // Generate IF statement
    public int getInstructionCount() {
        return instructions.size();
    }

    public List<String> getInstructionsFrom(int start) {
        return new ArrayList<>(instructions.subList(start, instructions.size()));
    }

    public void truncateInstructions(int start) {
        instructions.subList(start, instructions.size()).clear();
    }

    public void generateIf(
            Expr condition,
            List<String> thenStatements) {

        String conditionResult =
            generateExpression(condition);

        String trueLabel = newLabel();
        String endLabel = newLabel();

        instructions.add(
            "if " + conditionResult +
            " goto " + trueLabel
        );

        instructions.add(
            "goto " + endLabel
        );

        instructions.add(
            trueLabel + ":"
        );

        instructions.addAll(thenStatements);

        instructions.add(
            endLabel + ":"
        );
    }

    // Generate IF-ELSE statement
    public void generateIfElse(
            Expr condition,
            List<String> thenStatements,
            List<String> elseStatements) {

        String conditionResult =
            generateExpression(condition);

        String trueLabel = newLabel();
        String falseLabel = newLabel();
        String endLabel = newLabel();

        instructions.add(
            "if " + conditionResult +
            " goto " + trueLabel
        );

        instructions.add(
            "goto " + falseLabel
        );

        instructions.add(
            trueLabel + ":"
        );

        instructions.addAll(thenStatements);

        instructions.add(
            "goto " + endLabel
        );

        instructions.add(
            falseLabel + ":"
        );

        instructions.addAll(elseStatements);

        instructions.add(
            endLabel + ":"
        );
    }

    // Generate WHILE loop
    public void generateWhile(
            Expr condition,
            List<String> bodyStatements) {

        String startLabel = newLabel();
        String bodyLabel = newLabel();
        String endLabel = newLabel();

        instructions.add(
            startLabel + ":"
        );

        String conditionResult =
            generateExpression(condition);

        instructions.add(
            "if " + conditionResult +
            " goto " + bodyLabel
        );

        instructions.add(
            "goto " + endLabel
        );

        instructions.add(
            bodyLabel + ":"
        );

        instructions.addAll(bodyStatements);

        instructions.add(
            "goto " + startLabel
        );

        instructions.add(
            endLabel + ":"
        );
    }

    public void printTAC() {

        System.out.println(
            "\n--- Three Address Code ---"
        );

        for (String instruction : instructions) {
            System.out.println(instruction);
        }
    }

    public static void main(String[] args) {

        TACGenerator generator =
            new TACGenerator();

        System.out.println(
            "=== Control-Flow TAC Test ==="
        );

        /*
         * Test 1:
         *
         * if (a < b)
         *     x = 1;
         */

        Expr ifCondition =
            new Binary(
                new Variable("a"),
                "<",
                new Variable("b")
            );

        List<String> ifBody =
            new ArrayList<>();

        ifBody.add("x = 1");

        generator.generateIf(
            ifCondition,
            ifBody
        );

        /*
         * Test 2:
         *
         * if (a < b)
         *     x = 1;
         * else
         *     x = 2;
         */

        Expr ifElseCondition =
            new Binary(
                new Variable("a"),
                "<",
                new Variable("b")
            );

        List<String> thenBody =
            new ArrayList<>();

        thenBody.add("x = 1");

        List<String> elseBody =
            new ArrayList<>();

        elseBody.add("x = 2");

        generator.generateIfElse(
            ifElseCondition,
            thenBody,
            elseBody
        );

        /*
         * Test 3:
         *
         * while (a < 10)
         *     a = a + 1;
         */

        Expr whileCondition =
            new Binary(
                new Variable("a"),
                "<",
                new Literal("10")
            );

        List<String> whileBody =
            new ArrayList<>();

        whileBody.add("t = a + 1");
        whileBody.add("a = t");

        generator.generateWhile(
            whileCondition,
            whileBody
        );

        generator.printTAC();

        System.out.println(
            "\n=== Test Finished ==="
        );
    }
}