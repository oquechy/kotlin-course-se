grammar Exp;

file : block;

block : statement *;

blockWithBraces : '{' block '}';

statement
    : function
    | variable
    | expression
    | whileLoop
    | condition
    | assignment
    | returnStatement
    ;

function
    : 'fun' Identifier '(' parameterNames ')' blockWithBraces;

variable
    : 'var' Identifier ( '=' expression ) ?;

parameterNames
    : ( ( Identifier',' ) * Identifier ) ?;

whileLoop
    : 'while' '(' expression ')' blockWithBraces;

condition
    : 'if' '(' expression ')' blockWithBraces ( 'else' blockWithBraces ) ?;

assignment
    : Identifier '=' expression;

returnStatement
    : 'return' expression;

expression
    : orExpression;

atomExpression
    : functionCall
    | identifier
    | literal
    | expressionWithBraces
    ;

expressionWithBraces : '(' expression ')';

identifier : Identifier;

literal : Literal;

functionCall
    : Identifier '(' arguments ')';

arguments
    : ( ( expression ',' ) * expression ) ?;

/*
    Арифметическое выражение с операциями: +, -, *, /, %, >, <, >=, <=, ==, !=, ||, &&
    Семантика и приоритеты операций примерно как в Си
*/

orExpression
    : andExpression
    | andExpression OrOp orExpression
    ;

andExpression
    : relationalExpression
    | relationalExpression AndOp andExpression
    ;

relationalExpression
    : additiveExpression
    | additiveExpression RelationalOp relationalExpression
    ;

additiveExpression
    : multiplicativeExpression
    | multiplicativeExpression AdditiveOp additiveExpression
    ;

multiplicativeExpression
    : atomExpression
    | atomExpression MultiplicativeOp multiplicativeExpression
    ;

MultiplicativeOp : '*' | '/' | '%' ;

AdditiveOp : '+' | '-' ;

RelationalOp : '>' | '<' | '>=' | '<=' | '==' | '!=' ;

AndOp : '&&' ;

OrOp : '||' ;

/* Идентификатор как в Си */
Identifier : [a-zA-Z] ( [a-zA-Z] | [0-9] ) *;

/* Десятичный целочисленный литерал без ведущих нулей */
Literal : '0' | '-' ? [1-9] [0-9] *;

WS : ( '//'.*?'\n' | ' ' | '\t' | '\r' | '\n' ) -> skip;