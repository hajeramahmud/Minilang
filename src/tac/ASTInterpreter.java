
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

    private List<String> captureBlock() {
        int start = generator.getInstructionCount();

        while (position < lines.size()) {
            String line = lines.get(position).trim();

            if (line.equals("END_BLOCK")) {
                position++;
                List<String> result = generator.getInstructionsFrom(start);
                generator.truncateInstructions(start);
                return result;
            }

            processStatement();
        }

        throw new IllegalArgumentException("Missing END_BLOCK.");
    }

    private void processStatement() {
        String line = nextLine();

        if (line.startsWith("VAR_DECL ")) {
            String[] parts = line.split("\\s+", 3);
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid declaration: " + line);
            }
            analyzer.declareVariable(parts[2], parts[1]);

        } else if (line.startsWith("ASSIGN ")) {
            String variable = line.substring(7).trim();
            TypedExpr value = parseExpression();
            if (value.type != null
                    && analyzer.checkAssignment(variable, value.type)) {
                generator.generateAssignment(variable, value.expression);
            }

        } else if (line.equals("PRINT")) {
            TypedExpr value = parseExpression();
            if (value.type != null) {
                generator.generatePrint(value.expression);
            }

        } else if (line.equals("IF")) {
            TypedExpr condition = parseExpression();

            if (!nextLine().equals("THEN")) {
                throw new IllegalArgumentException("Expected THEN after IF condition.");
            }
            if (!nextLine().equals("BLOCK")) {
                throw new IllegalArgumentException("Expected BLOCK after THEN.");
            }

            List<String> thenStatements = captureBlock();
            String marker = nextLine();

            if (!"bool".equals(condition.type)) {
                throw new IllegalArgumentException("IF condition must have bool type.");
            }

            if (marker.equals("ELSE")) {
                if (!nextLine().equals("BLOCK")) {
                    throw new IllegalArgumentException("Expected BLOCK after ELSE.");
                }

                List<String> elseStatements = captureBlock();
                if (!nextLine().equals("END_IF")) {
                    throw new IllegalArgumentException("Expected END_IF.");
                }

                generator.generateIfElse(
                        condition.expression, thenStatements, elseStatements
                );

            } else if (marker.equals("END_IF")) {
                generator.generateIf(condition.expression, thenStatements);

            } else {
                throw new IllegalArgumentException(
                        "Expected ELSE or END_IF, found: " + marker
                );
            }

        } else if (line.equals("WHILE")) {
            TypedExpr condition = parseExpression();

            if (!"bool".equals(condition.type)) {
                throw new IllegalArgumentException("WHILE condition must have bool type.");
            }
            if (!nextLine().equals("BLOCK")) {
                throw new IllegalArgumentException("Expected BLOCK after WHILE condition.");
            }

            List<String> bodyStatements = captureBlock();
            if (!nextLine().equals("END_WHILE")) {
                throw new IllegalArgumentException("Expected END_WHILE.");
            }

            generator.generateWhile(condition.expression, bodyStatements);

        } else {
            throw new IllegalArgumentException("Unsupported statement: " + line);
        }
    }

    public void process() {
        while (position < lines.size()) {
            String line = lines.get(position).trim();

            if (line.equals("PROGRAM") || line.equals("END_PROGRAM")) {
                position++;
                continue;
            }

            processStatement();
        }

        analyzer.printSymbolTable();

        if (analyzer.hasErrors()) {
            System.out.println("\nSemantic analysis failed. TAC not displayed.");
        } else {
            System.out.println("\nSemantic analysis successful.");
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
