
package tac;

import semantic.SemanticAnalyzer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ASTInterpreter {

    private final List<String> lines;
    private int position = 0;

    private final SemanticAnalyzer analyzer =
            new SemanticAnalyzer();

    private final TACGenerator generator =
            new TACGenerator();

    private static class TypedExpr {
        TACGenerator.Expr expression;
        String type;

        TypedExpr(TACGenerator.Expr expression, String type) {
            this.expression = expression;
            this.type = type;
        }
    }

    public ASTInterpreter(List<String> lines) {
        this.lines = lines;
    }

    private String nextLine() {
        if (position >= lines.size()) {
            throw new IllegalArgumentException(
                    "Unexpected end of AST."
            );
        }

        return lines.get(position++).trim();
    }

    private TypedExpr parseExpression() {
        String line = nextLine();

        if (line.startsWith("NUMBER ")) {
            String value = line.substring(7).trim();
            String type = value.contains(".") ? "float" : "int";

            return new TypedExpr(
                    new TACGenerator.Literal(value), type
            );
        }

        if (line.startsWith("IDENTIFIER ")) {
            String name = line.substring(11).trim();
            String type = analyzer.getType(name);

            return new TypedExpr(
                    new TACGenerator.Variable(name), type
            );
        }

        if (line.startsWith("BINARY ")) {
            String operator = line.substring(7).trim();

            TypedExpr left = parseExpression();
            TypedExpr right = parseExpression();

            String resultType = null;

            if (left.type != null && right.type != null) {
                resultType = analyzer.checkBinaryOperation(
                        left.type, operator, right.type
                );
            }

            return new TypedExpr(
                    new TACGenerator.Binary(
                            left.expression,
                            operator,
                            right.expression
                    ),
                    resultType
            );
        }

        throw new IllegalArgumentException(
                "Unsupported expression: " + line
        );
    }

    public void process() {
        while (position < lines.size()) {
            String line = nextLine();

            if (line.equals("PROGRAM")
                    || line.equals("END_PROGRAM")) {
                continue;
            }

            if (line.startsWith("VAR_DECL ")) {
                String[] parts = line.split("\\s+", 3);

                if (parts.length != 3) {
                    throw new IllegalArgumentException(
                            "Invalid declaration: " + line
                    );
                }

                analyzer.declareVariable(parts[2], parts[1]);
            } else if (line.startsWith("ASSIGN ")) {
                String variable = line.substring(7).trim();
                TypedExpr value = parseExpression();

                if (value.type != null
                        && analyzer.checkAssignment(
                                variable, value.type)) {
                    generator.generateAssignment(
                            variable, value.expression
                    );
                }
            } else if (line.equals("PRINT")) {
                TypedExpr value = parseExpression();

                if (value.type != null) {
                    generator.generatePrint(value.expression);
                }
            } else {
                throw new IllegalArgumentException(
                        "Unsupported statement: " + line
                );
            }
        }

        analyzer.printSymbolTable();

        if (analyzer.hasErrors()) {
            System.out.println(
                    "\nSemantic analysis failed. TAC not displayed."
            );
        } else {
            System.out.println(
                    "\nSemantic analysis successful."
            );
            generator.printTAC();
        }
    }

    public static void main(String[] args) {
        String filename = args.length > 0
                ? args[0] : "ast.txt";

        try {
            List<String> lines =
                    Files.readAllLines(Path.of(filename));

            ASTInterpreter interpreter =
                    new ASTInterpreter(lines);

            interpreter.process();
        } catch (IOException e) {
            System.err.println(
                    "Could not read AST file: " + filename
            );
        } catch (IllegalArgumentException e) {
            System.err.println(
                    "AST processing error: " + e.getMessage()
            );
        }
    }
}
