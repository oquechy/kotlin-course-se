grammar Exp;

file : block;

block :  statement *;

blockWithBraces : '{' block '}';

statement
    : expression
    | function
    | returnStatement
    | variable
    | whileLoop
    | condition
    | assignment
    ;

function
    : 'fun' Identifier '(' parameterNames ')' '{' block '}';

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
    | literal
    | identifier
    | expressionWithBraces
    ;

expressionWithBraces : '(' expression ')';

identifier : Identifier;

literal : Literal;

functionCall
    : Identifier '(' arguments ')';

arguments
    : ( expression ( ',' expression ) * ) ?;

/*
    Арифметическое выражение с операциями: +, -, *, /, %, >, <, >=, <=, ==, !=, ||, &&
    Семантика и приоритеты операций примерно как в Си
*/

orExpression
    : andExpression OrOp orExpression
    | andExpression
    ;

andExpression
    : relationalExpression AndOp andExpression
    | relationalExpression
    ;

relationalExpression
    : additiveExpression RelationalOp relationalExpression
    | additiveExpression
    ;

additiveExpression
    : multiplicativeExpression AdditiveOp additiveExpression
    | multiplicativeExpression
    ;

multiplicativeExpression
    : atomExpression MultiplicativeOp multiplicativeExpression
    | atomExpression
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