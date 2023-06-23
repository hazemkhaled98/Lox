package jlox.parser;

import jlox.Jlox;
import jlox.scanner.Token;
import jlox.scanner.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    public List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(parseDeclaration());
        }
        return statements;
    }

    private Stmt parseDeclaration(){
        try {
            if(match(TokenType.VAR)) return parseVarDeclaration();
            if(match(TokenType.FUN)) return parseFunction("function");
            if(match(TokenType.CLASS)) return parseClassDeclaration();
            return parseStatement();
        } catch (ParseError error){
            synchronize();
            return null;
        }
    }

    private Stmt parseVarDeclaration(){
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");

        Expr initializer = null;
        if(match(TokenType.EQUAL))
            initializer = parseExpression();

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt.Function parseFunction(String type){
        Token name = consume(TokenType.IDENTIFIER, "Expected " + type + " name.");
        consume(TokenType.LEFT_PREN, "'(' is expected after " + type + " name.");
        List<Token> params = new ArrayList<>();
        if(!check(TokenType.RIGHT_PAREN)){
            do {
                if(params.size() >= 255)
                    error(peek(), "Can't have more than 255 parameters");

                params.add(consume(TokenType.IDENTIFIER, "Expected parameter name"));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "')' is expected after parameters");
        consume(TokenType.LEFT_BRACE, "'{' is expected before " + type + " body");
        List<Stmt> body = parseBlock();
        return new Stmt.Function(name, params, body);
    }

    private Stmt parseClassDeclaration(){
        Token name = consume(TokenType.IDENTIFIER, "Class name is expected.");

        Expr.Variable superClass = null;
        if(match(TokenType.LESS)){
            consume(TokenType.IDENTIFIER, "SuperClass name is expected.");
            superClass = new Expr.Variable(previous());
        }

        consume(TokenType.LEFT_BRACE, "Expected '{' after class name.");

        List<Stmt.Function> methods = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            methods.add(parseFunction("method"));
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after class body.");

        return new Stmt.Class(name, superClass, methods);
    }

    private Stmt parseStatement(){
        if(match(TokenType.FOR)) return parseForStatement();
        if(match(TokenType.IF)) return parseIfStatement();
        if(match(TokenType.WHILE)) return parseWhileStatement();
        if(match(TokenType.LEFT_BRACE)) return new Stmt.Block(parseBlock());
        if(match(TokenType.RETURN)) return parseReturnStatement();
        return parseExpressionStatement();
    }

    private Stmt parseForStatement(){
        consume(TokenType.LEFT_PREN, "'(' is expected after 'for'.");

        Stmt initializer;
        if(match(TokenType.SEMICOLON))
            initializer = null;
        else if(match(TokenType.VAR))
            initializer = parseVarDeclaration();
        else
            initializer = parseExpressionStatement();

        Expr condition = null;
        if(!check(TokenType.SEMICOLON))
            condition = parseExpression();
        consume(TokenType.SEMICOLON, "';' is expected after condition.");

        Expr increment = null;
        if(!check(TokenType.RIGHT_PAREN))
            increment = parseExpression();
        consume(TokenType.RIGHT_PAREN, "')' is expected after for clauses.");

        Stmt body = parseStatement();

        if(increment != null)
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));

        if(condition == null)
            condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if(initializer != null)
            body = new Stmt.Block(Arrays.asList(initializer, body));

        return body;
    }


    private Stmt parseIfStatement(){
        consume(TokenType.LEFT_PREN, "Expected '(' after 'if'.");
        Expr condition = parseOr();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition.");

        Stmt thenBranch = parseStatement();
        Stmt elseBranch = null;
        if(match(TokenType.ELSE)){
            elseBranch = parseStatement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }


    private Stmt parseWhileStatement(){
        consume(TokenType.LEFT_PREN, "'(' was expected after 'while'.");
        Expr condition = parseExpression();
        consume(TokenType.RIGHT_PAREN, "')' was expected after condition.");
        Stmt body = parseStatement();
        return new Stmt.While(condition, body);
    }

    private List<Stmt> parseBlock(){
        List<Stmt> statements = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd())
            statements.add(parseDeclaration());

        consume(TokenType.RIGHT_BRACE, "'}' was expected.");

        return statements;
    }

    private Stmt parseReturnStatement(){
        Token keyword = previous();
        Expr value = null;
        if(!check(TokenType.SEMICOLON))
            value = parseExpression();
        consume(TokenType.SEMICOLON, "';' is expected after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt parseExpressionStatement(){
        Expr expr = parseExpression();
        consume(TokenType.SEMICOLON, "';' was expected.");
        return new Stmt.Expression(expr);
    }
    private Expr parseExpression(){
        return parseAssignment();
    }

    private Expr parseAssignment(){
        Expr expr = parseOr();

        if(match(TokenType.EQUAL)){
            Token equals = previous();
            Expr value = parseAssignment();

            if(expr instanceof Expr.Variable variable){
                Token name = variable.getName();
                return new Expr.Assign(name, value);
            }

            else if(expr instanceof Expr.Get getExpr){
                return new Expr.Set(getExpr.getObject(), getExpr.getName(), value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr parseOr(){
        Expr expr = parseAnd();

        while (match(TokenType.OR)){
            Token operator = previous();
            Expr right = parseAnd();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr parseAnd(){
        Expr expr = parseEquality();

        while(match(TokenType.AND)){
            Token operator = previous();
            Expr right = parseEquality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr parseEquality(){
        Expr expr = parseComparison();

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = parseComparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr parseComparison(){
        Expr expr = parseTerm();

        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)){
            Token operator = previous();
            Expr right = parseTerm();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr parseTerm(){
        Expr expr = parseFactor();

        while(match(TokenType.MINUS, TokenType.PLUS)){
            Token operator = previous();
            Expr right = parseFactor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr parseFactor(){
        Expr expr = parseUnary();

        while(match(TokenType.SLASH, TokenType.STAR)){
            Token operator = previous();
            Expr right = parseUnary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr parseUnary(){
        if(match(TokenType.BANG, TokenType.MINUS)){
            Token operator = previous();
            Expr right = parseUnary();
            return new Expr.Unary(operator, right);
        }
        return parseCall();
    }

    private Expr parseCall(){
        Expr expr = parsePrimary();

        while (true){
            if(match(TokenType.LEFT_PREN))
                expr = finishCall(expr);
            else if(match(TokenType.DOT)){
                Token name = consume(TokenType.IDENTIFIER, "Property name is expected after '.' operator.");
                expr = new Expr.Get(expr, name);
            }
            else break;
        }

        return expr;
    }

    private Expr parsePrimary(){
        if(match(TokenType.FALSE)) return new Expr.Literal(false);
        if(match(TokenType.TRUE)) return new Expr.Literal(true);
        if(match(TokenType.NIL)) return new Expr.Literal(null);

        if(match(TokenType.NUMBER, TokenType.STRING))
            return new Expr.Literal(previous().getLiteral());

        if(match(TokenType.SUPER)){
            Token keyword = previous();
            consume(TokenType.DOT, "Expected '.' after 'super'.");
            Token method = consume(TokenType.IDENTIFIER, "Expected superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if(match(TokenType.THIS)) return new Expr.This(previous());

        if(match(TokenType.IDENTIFIER))
            return new Expr.Variable(previous());

        if(match(TokenType.LEFT_PREN)){
            Expr expr = parseExpression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expression expected.");
    }

    private boolean match(TokenType ...types){
        for(TokenType type : types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type){
        if(isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance(){
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd(){
        return peek().getType() == TokenType.EOF;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message){
        if(check(type)) return advance();

        throw error(peek(), message);
    }

    private Expr finishCall(Expr callee){
        List<Expr> arguments = new ArrayList<>();
        if(!check(TokenType.RIGHT_PAREN)){
            do {
                if(arguments.size() >= 255)
                    error(peek(), "Can't have more than 255 arguments.");
                arguments.add(parseExpression());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "')' is expected after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private ParseError error(Token token, String message){
        Jlox.error(token, message);

        return new ParseError();
    }


    private void synchronize(){
        advance();
        while (!isAtEnd()){
            if(previous().getType() == TokenType.SEMICOLON) return;
            switch (peek().getType()){
                case CLASS, FUN, FOR, IF, WHILE, RETURN -> {
                    return;
                }
            }
            advance();
        }
    }
}
