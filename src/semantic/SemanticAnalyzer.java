package semantic;

import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer {

    // Symbol table:
    // variable name -> data type
    private final Map<String, String> symbolTable = new HashMap<>();

    private boolean hasError = false;

    // Declare a new variable
    public void declareVariable(String name, String type) {
        if (symbolTable.containsKey(name)) {
            reportError("Variable '" + name + "' is already declared.");
            return;
        }

        symbolTable.put(name, type);
        System.out.println("Declared: " + type + " " + name);
    }

    // Check whether a variable exists
    public boolean isDeclared(String name) {
        return symbolTable.containsKey(name);
    }

    // Get the type of a variable
    public String getType(String name) {
        if (!isDeclared(name)) {
            reportError("Variable '" + name + "' is not declared.");
            return null;
        }

        return symbolTable.get(name);
    }

    // Check assignment compatibility
    public boolean checkAssignment(String variableName, String valueType) {

        if (!isDeclared(variableName)) {
            reportError(
                "Cannot assign to undeclared variable '" + variableName + "'."
            );
            return false;
        }

        String variableType = symbolTable.get(variableName);

        if (!isTypeCompatible(variableType, valueType)) {
            reportError(
                "Type mismatch: cannot assign " +
                valueType +
                " to " +
                variableType +
                " variable '" +
                variableName +
                "'."
            );
            return false;
        }

        System.out.println(
            "Valid assignment: " +
            variableName +
            " = (" +
            valueType +
            ")"
        );

        return true;
    }

    // Check whether two types are compatible
    private boolean isTypeCompatible(String targetType, String sourceType) {

        if (targetType == null || sourceType == null) {
            return false;
        }

        // Same types are always compatible
        if (targetType.equals(sourceType)) {
            return true;
        }

        // Allow int -> float conversion
        if (targetType.equals("float") && sourceType.equals("int")) {
            return true;
        }

        return false;
    }

    // Check whether an operation is valid
    public String checkBinaryOperation(
            String leftType,
            String operator,
            String rightType) {

        if (leftType == null || rightType == null) {
            return null;
        }

        // Arithmetic operators
        if (operator.equals("+") ||
            operator.equals("-") ||
            operator.equals("*") ||
            operator.equals("/")) {

            if (!isNumeric(leftType) || !isNumeric(rightType)) {
                reportError(
                    "Arithmetic operator '" +
                    operator +
                    "' requires numeric operands."
                );
                return null;
            }

            // If either operand is float, result is float
            if (leftType.equals("float") || rightType.equals("float")) {
                return "float";
            }

            return "int";
        }

        // Relational operators
        if (operator.equals("<") ||
            operator.equals(">") ||
            operator.equals("<=") ||
            operator.equals(">=") ||
            operator.equals("==") ||
            operator.equals("!=")) {

            if (!areComparable(leftType, rightType)) {
                reportError(
                    "Cannot compare " +
                    leftType +
                    " and " +
                    rightType +
                    "."
                );
                return null;
            }

            return "bool";
        }

        // Logical operators
        if (operator.equals("&&") ||
            operator.equals("||")) {

            if (!leftType.equals("bool") ||
                !rightType.equals("bool")) {

                reportError(
                    "Logical operator '" +
                    operator +
                    "' requires boolean operands."
                );

                return null;
            }

            return "bool";
        }

        reportError("Unknown operator: " + operator);
        return null;
    }

    // Check unary operation
    public String checkUnaryOperation(
            String operator,
            String operandType) {

        if (operator.equals("!")) {

            if (!operandType.equals("bool")) {
                reportError(
                    "Logical NOT '!' requires a boolean operand."
                );
                return null;
            }

            return "bool";
        }

        if (operator.equals("-")) {

            if (!isNumeric(operandType)) {
                reportError(
                    "Unary '-' requires a numeric operand."
                );
                return null;
            }

            return operandType;
        }

        reportError("Unknown unary operator: " + operator);
        return null;
    }

    // Check numeric type
    private boolean isNumeric(String type) {
        return type.equals("int") || type.equals("float");
    }

    // Check whether two types can be compared
    private boolean areComparable(String leftType, String rightType) {

        if (leftType.equals(rightType)) {
            return true;
        }

        // int and float can be compared
        return isNumeric(leftType) && isNumeric(rightType);
    }

    // Display semantic error
    private void reportError(String message) {
        hasError = true;
        System.out.println("Semantic Error: " + message);
    }

    // Check whether any error occurred
    public boolean hasErrors() {
        return hasError;
    }

    // Display current symbol table
    public void printSymbolTable() {

        System.out.println("\n--- Symbol Table ---");

        if (symbolTable.isEmpty()) {
            System.out.println("Symbol table is empty.");
            return;
        }

        for (Map.Entry<String, String> entry : symbolTable.entrySet()) {
            System.out.println(
                entry.getKey() + " : " + entry.getValue()
            );
        }
    }

    // Simple test
    public static void main(String[] args) {

        SemanticAnalyzer analyzer = new SemanticAnalyzer();

        System.out.println("=== Semantic Analyzer Test ===");

        // Declarations
        analyzer.declareVariable("age", "int");
        analyzer.declareVariable("price", "float");
        analyzer.declareVariable("isStudent", "bool");

        // Duplicate declaration
        analyzer.declareVariable("age", "int");

        // Valid assignments
        analyzer.checkAssignment("age", "int");
        analyzer.checkAssignment("price", "float");

        // int -> float is allowed
        analyzer.checkAssignment("price", "int");

        // Invalid assignment
        analyzer.checkAssignment("age", "float");

        // Undeclared variable
        analyzer.checkAssignment("marks", "int");

        // Binary operations
        System.out.println(
            "\nint + int = " +
            analyzer.checkBinaryOperation("int", "+", "int")
        );

        System.out.println(
            "int + float = " +
            analyzer.checkBinaryOperation("int", "+", "float")
        );

        System.out.println(
            "int < float = " +
            analyzer.checkBinaryOperation("int", "<", "float")
        );

        System.out.println(
            "bool && bool = " +
            analyzer.checkBinaryOperation("bool", "&&", "bool")
        );

        // Invalid operation
        System.out.println(
            "bool + int = " +
            analyzer.checkBinaryOperation("bool", "+", "int")
        );

        // Symbol table
        analyzer.printSymbolTable();

        System.out.println("\n=== Test Finished ===");

        if (analyzer.hasErrors()) {
            System.out.println("Semantic errors were found.");
        } else {
            System.out.println("No semantic errors found.");
        }
    }
}