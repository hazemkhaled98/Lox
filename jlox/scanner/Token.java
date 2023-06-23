package jlox.scanner;

public class Token {
    final private TokenType type;
    final private String lexeme;

    final private Object literal;

    final private int line;

    Token(TokenType type, String lexeme, Object literal, int line){
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString(){
        return type + " " + lexeme + " " + literal;
    }

    public TokenType getType(){
        return type;
    }

    public Object getLiteral(){
        return literal;
    }

    public int getLine(){
        return line;
    }

    public String getLexeme(){
        return lexeme;
    }
}
