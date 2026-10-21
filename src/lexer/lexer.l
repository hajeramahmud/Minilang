%{
#include <stdio.h>
%}

%option noyywrap

DIGIT   [0-9]
LETTER  [a-zA-Z_]
ID      {LETTER}({LETTER}|{DIGIT})*

%%

"int"|"float"|"bool"|"if"|"else"|"while"|"print" {
    printf("KEYWORD: %s\n", yytext);
}

"=="|"!="|"<="|">="|"&&"|"||" {
    printf("OPERATOR: %s\n", yytext);
}

[+\-*/%<>=!] {
    printf("OPERATOR: %s\n", yytext);
}

{DIGIT}+"."{DIGIT}+ {
    printf("FLOAT: %s\n", yytext);
}

{DIGIT}+ {
    printf("INTEGER: %s\n", yytext);
}

{ID} {
    printf("IDENTIFIER: %s\n", yytext);
}

[;(),{}] {
    printf("DELIMITER: %s\n", yytext);
}

[ \t\r\n]+   { /* Ignore whitespace */ }

. {
    printf("LEXICAL ERROR: %s\n", yytext);
}

%%

int main(void)
{
    printf("Enter MiniLang code:\n");
    yylex();
    return 0;
}