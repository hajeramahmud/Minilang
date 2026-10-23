#include <iostream>
#include "parser.tab.hpp"
#include "ast_exporter.hpp"

extern Program* root;

int main() {
    std::cout << "Enter MiniLang code (Ctrl+D to finish):\n";

    int result = yyparse();

    if (result == 0) {
        std::cout << "Parsing successful!\n";

        ASTExporter exporter;

        if (exporter.exportProgram(root, "ast.txt")) {
            std::cout << "AST exported to ast.txt\n";
        } else {
            std::cerr << "Failed to export AST.\n";
        }

        delete root;
        return 0;
    }

    std::cout << "Parsing failed.\n";
    delete root;
    return 1;
}
