#ifndef AST_HPP
#define AST_HPP

#include <iostream>
#include <string>
#include <vector>
using namespace std;

class ASTNode {
public:
    virtual ~ASTNode() = default;
};

class Expr : public ASTNode {
public:
    virtual ~Expr() = default;
};

class Stmt : public ASTNode {
public:
    virtual ~Stmt() = default;
};

class NumberExpr : public Expr {
public:
    string value;
    explicit NumberExpr(string v) : value(v) {}
};

class IdentifierExpr : public Expr {
public:
    string name;
    explicit IdentifierExpr(string n) : name(n) {}
};

class BinaryExpr : public Expr {
public:
    string op;
    Expr* left;
    Expr* right;

    BinaryExpr(string o, Expr* l, Expr* r)
        : op(o), left(l), right(r) {}

    ~BinaryExpr() override {
        delete left;
        delete right;
    }
};

class UnaryExpr : public Expr {
public:
    string op;
    Expr* operand;

    UnaryExpr(string o, Expr* e) : op(o), operand(e) {}

    ~UnaryExpr() override {
        delete operand;
    }
};

class VarDecl : public Stmt {
public:
    string type;
    string name;

    VarDecl(string t, string n) : type(t), name(n) {}
};

class AssignStmt : public Stmt {
public:
    string name;
    Expr* value;

    AssignStmt(string n, Expr* v) : name(n), value(v) {}

    ~AssignStmt() override {
        delete value;
    }
};

class PrintStmt : public Stmt {
public:
    Expr* value;

    explicit PrintStmt(Expr* v) : value(v) {}

    ~PrintStmt() override {
        delete value;
    }
};

class BlockStmt : public Stmt {
public:
    vector<Stmt*> statements;

    ~BlockStmt() override {
        for (Stmt* stmt : statements)
            delete stmt;
    }
};

class IfStmt : public Stmt {
public:
    Expr* condition;
    Stmt* thenBranch;
    Stmt* elseBranch;

    IfStmt(Expr* c, Stmt* t, Stmt* e = nullptr)
        : condition(c), thenBranch(t), elseBranch(e) {}

    ~IfStmt() override {
        delete condition;
        delete thenBranch;
        delete elseBranch;
    }
};

class WhileStmt : public Stmt {
public:
    Expr* condition;
    Stmt* body;

    WhileStmt(Expr* c, Stmt* b) : condition(c), body(b) {}

    ~WhileStmt() override {
        delete condition;
        delete body;
    }
};

class Program : public ASTNode {
public:
    vector<Stmt*> statements;

    ~Program() override {
        for (Stmt* stmt : statements)
            delete stmt;
    }
};

#endif
