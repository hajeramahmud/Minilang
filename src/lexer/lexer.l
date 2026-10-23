
%{
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../parser/parser.tab.hpp"
%}

%option noyywrap

DIGIT   [0-9]
LETTER  [a-zA-Z_]
ID      {LETTER}({LETTER}|{DIGIT})*

%%

"int"       { return INT; }
"float"     { return FLOAT; }
"bool"      { return BOOL; }
"if"        { return IF; }
"else"      { return ELSE; }
"while"     { return WHILE; }
"print"     { return PRINT; }

"=="        { return EQ; }
"!="        { return NE; }
"<="        { return LE; }
">="        { return GE; }
"&&"        { return AND; }
"||"        { return OR; }

{DIGIT}+"."{DIGIT}+ {
    yylval.text = strdup(yytext);
    return FLOAT_LITERAL;
}

{DIGIT}+ {
    yylval.text = strdup(yytext);
    return INTEGER_LITERAL;
}

{ID} {
    yylval.text = strdup(yytext);
    return IDENTIFIER;
}

[+\-*/%<>=!;(),{}] {
    return yytext[0];
}

[ \t\r\n]+  { /* Ignore whitespace */ }

. {
    fprintf(stderr, "Lexical error: unexpected character '%s'\n", yytext);
    return yytext[0];
}

%%
