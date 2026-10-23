#ifndef AST_EXPORTER_HPP
#define AST_EXPORTER_HPP

#include <fstream>
#include <string>
#include "../ast/ast.hpp"

using namespace std;

class ASTExporter {
private:
    ofstream out;

    void exportExpr(Expr* expr) {
        if (auto number = dynamic_cast<NumberExpr*>(expr)) {
            out << "NUMBER " << number->value << "\n";
        }
        else if (auto identifier = dynamic_cast<IdentifierExpr*>(expr)) {
            out << "IDENTIFIER " << identifier->name << "\n";
        }
        else if (auto binary = dynamic_cast<BinaryExpr*>(expr)) {
            out << "BINARY " << binary->op << "\n";
            exportExpr(binary->left);
            exportExpr(binary->right);
        }
        else if (auto unary = dynamic_cast<UnaryExpr*>(expr)) {
            out << "UNARY " << unary->op << "\n";
            exportExpr(unary->operand);
        }
    }

    void exportStmt(Stmt* stmt) {
        if (auto decl = dynamic_cast<VarDecl*>(stmt)) {
            out << "VAR_DECL "
                << decl->type << " "
                << decl->name << "\n";
        }
        else if (auto assign = dynamic_cast<AssignStmt*>(stmt)) {
            out << "ASSIGN " << assign->name << "\n";
            exportExpr(assign->value);
        }
        else if (auto print = dynamic_cast<PrintStmt*>(stmt)) {
            out << "PRINT\n";
            exportExpr(print->value);
        }
        else if (auto block = dynamic_cast<BlockStmt*>(stmt)) {
            out << "BLOCK\n";

            for (Stmt* child : block->statements) {
                exportStmt(child);
            }

            out << "END_BLOCK\n";
        }
        else if (auto ifStmt = dynamic_cast<IfStmt*>(stmt)) {
            out << "IF\n";

            exportExpr(ifStmt->condition);

            out << "THEN\n";
            exportStmt(ifStmt->thenBranch);

            if (ifStmt->elseBranch != nullptr) {
                out << "ELSE\n";
                exportStmt(ifStmt->elseBranch);
            }

            out << "END_IF\n";
        }
        else if (auto whileStmt = dynamic_cast<WhileStmt*>(stmt)) {
            out << "WHILE\n";

            exportExpr(whileStmt->condition);
            exportStmt(whileStmt->body);

            out << "END_WHILE\n";
        }
    }

public:
    bool exportProgram(Program* program,
                       const string& filename) {

        out.open(filename);

        if (!out.is_open()) {
            return false;
        }

        out << "PROGRAM\n";

        for (Stmt* stmt : program->statements) {
            exportStmt(stmt);
        }

        out << "END_PROGRAM\n";

        out.close();

        return true;
    }
};

#endif
