
#include <iostream>
#include "parser.tab.hpp"

extern Program* root;

int main() {
    std::cout << "Enter MiniLang code (Ctrl+D to finish):\n";

    int result = yyparse();

    if (result == 0) {
        std::cout << "Parsing successful!\n";
        delete root;
        return 0;
    }

    std::cout << "Parsing failed.\n";
    delete root;
    return 1;
}
