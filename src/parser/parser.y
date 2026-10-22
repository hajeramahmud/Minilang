
%code requires {
    #include "../ast/ast.hpp"
}

%{
#include <iostream>
#include <string>
#include <utility>
#include "../ast/ast.hpp"

using namespace std;

int yylex(void);
void yyerror(const char *s);

Program* root = nullptr;
%}

%define parse.error detailed

%union {
    char* text;
    Expr* expr;
    Stmt* stmt;
    Program* program;
    BlockStmt* block;
}

%token INT FLOAT BOOL IF ELSE WHILE PRINT
%token <text> IDENTIFIER INTEGER_LITERAL FLOAT_LITERAL
%token EQ NE LE GE AND OR


%type <program> program
%type <stmt> statement declaration assignment print_statement if_statement while_statement
%type <block> statement_list block
%type <expr> expression

%left OR
%left AND
%left EQ NE
%left '<' '>' LE GE
%left '+' '-'
%left '*' '/' '%'
%right '!' UMINUS

%start program

%%

program:
    statement_list
    {
        root = new Program();
        root->statements = std::move($1->statements);
        delete $1;
    }
;

statement_list:
    %empty
    {
        $$ = new BlockStmt();
    }
  | statement_list statement
    {
        $1->statements.push_back($2);
        $$ = $1;
    }
;

statement:
    declaration       { $$ = $1; }
  | assignment        { $$ = $1; }
  | print_statement   { $$ = $1; }
  | if_statement      { $$ = $1; }
  | while_statement  { $$ = $1; }
  | block             { $$ = $1; }
;

declaration:
    INT IDENTIFIER ';'
    {
        $$ = new VarDecl("int", $2);
        free($2);
    }
  | FLOAT IDENTIFIER ';'
    {
        $$ = new VarDecl("float", $2);
        free($2);
    }
  | BOOL IDENTIFIER ';'
    {
        $$ = new VarDecl("bool", $2);
        free($2);
    }
;

assignment:
    IDENTIFIER '=' expression ';'
    {
        $$ = new AssignStmt($1, $3);
        free($1);
    }
;

print_statement:
    PRINT '(' expression ')' ';'
    {
        $$ = new PrintStmt($3);
    }
;

if_statement:
    IF '(' expression ')' statement
    {
        $$ = new IfStmt($3, $5);
    }
  | IF '(' expression ')' statement ELSE statement
    {
        $$ = new IfStmt($3, $5, $7);
    }
;

while_statement:
    WHILE '(' expression ')' statement
    {
        $$ = new WhileStmt($3, $5);
    }
;

block:
    '{' statement_list '}'
    {
        $$ = $2;
    }
;

expression:
    INTEGER_LITERAL
    {
        $$ = new NumberExpr($1);
        free($1);
    }
  | FLOAT_LITERAL
    {
        $$ = new NumberExpr($1);
        free($1);
    }
  | IDENTIFIER
    {
        $$ = new IdentifierExpr($1);
        free($1);
    }
  | '(' expression ')'
    {
        $$ = $2;
    }
  | expression '+' expression
    {
        $$ = new BinaryExpr("+", $1, $3);
    }
  | expression '-' expression
    {
        $$ = new BinaryExpr("-", $1, $3);
    }
  | expression '*' expression
    {
        $$ = new BinaryExpr("*", $1, $3);
    }
  | expression '/' expression
    {
        $$ = new BinaryExpr("/", $1, $3);
    }
  | expression '%' expression
    {
        $$ = new BinaryExpr("%", $1, $3);
    }
  | expression '<' expression
    {
        $$ = new BinaryExpr("<", $1, $3);
    }
  | expression '>' expression
    {
        $$ = new BinaryExpr(">", $1, $3);
    }
  | expression EQ expression
    {
        $$ = new BinaryExpr("==", $1, $3);
    }
  | expression NE expression
    {
        $$ = new BinaryExpr("!=", $1, $3);
    }
  | expression LE expression
    {
        $$ = new BinaryExpr("<=", $1, $3);
    }
  | expression GE expression
    {
        $$ = new BinaryExpr(">=", $1, $3);
    }
  | expression AND expression
    {
        $$ = new BinaryExpr("&&", $1, $3);
    }
  | expression OR expression
    {
        $$ = new BinaryExpr("||", $1, $3);
    }
  | '!' expression
    {
        $$ = new UnaryExpr("!", $2);
    }
  | '-' expression %prec UMINUS
    {
        $$ = new UnaryExpr("-", $2);
    }
;

%%

void yyerror(const char *s)
{
    cerr << "Parser error: " << s << '\n';
}
