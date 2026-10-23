
#ifndef SYMBOL_TABLE_HPP
#define SYMBOL_TABLE_HPP

#include <iostream>
#include <string>
#include <unordered_map>
#include <vector>
using namespace std;

class SymbolTable {
private:
    vector<unordered_map<string, string>> scopes;

public:
    SymbolTable() {
        enterScope();
    }

    void enterScope() {
        scopes.push_back(unordered_map<string, string>());
    }

    void exitScope() {
        if (!scopes.empty()) {
            scopes.pop_back();
        }
    }

    bool declareVariable(const string& name, const string& type) {
        if (scopes.empty()) {
            enterScope();
        }

        auto& currentScope = scopes.back();

        if (currentScope.find(name) != currentScope.end()) {
            cerr << "Error: Variable '" << name
                 << "' is already declared in this scope.\n";
            return false;
        }

        currentScope[name] = type;
        return true;
    }

    bool isDeclared(const string& name) const {
        for (auto it = scopes.rbegin(); it != scopes.rend(); ++it) {
            if (it->find(name) != it->end()) {
                return true;
            }
        }

        return false;
    }

    string getType(const string& name) const {
        for (auto it = scopes.rbegin(); it != scopes.rend(); ++it) {
            auto found = it->find(name);

            if (found != it->end()) {
                return found->second;
            }
        }

        return "";
    }

    void printSymbols() const {
        cout << "\nSymbol Table:\n";

        for (size_t i = 0; i < scopes.size(); ++i) {
            cout << "Scope " << i << ":\n";

            for (const auto& entry : scopes[i]) {
                cout << "  " << entry.first
                     << " : " << entry.second << '\n';
            }
        }
    }
};

#endif

