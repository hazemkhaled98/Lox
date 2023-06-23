package jlox.scanner;

import jlox.Jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    public Scanner(String source){
        this.source = source;
    }

    public List<Token> scanTokens(){
        while(!isAtEnd()){
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }


    private void scanToken(){
        char ch = advance();
        switch (ch) {
            case '(' -> addToken(TokenType.LEFT_PREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);
            case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL: TokenType.BANG);
            case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL: TokenType.EQUAL);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL: TokenType.LESS);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL: TokenType.GREATER);
            case '/' -> {
                if(match('/')){
                    while(peek() != '\n' && !isAtEnd()) advance();
                }
                else addToken(TokenType.SLASH);
            }
            // ignore whitespaces
            case ' ', '\r', '\t' -> {}
            case '\n' -> line++;
            case '"' -> scanString();
            default -> {
                if(isDigit(ch)){
                    scanNumber();
                }
                else if(isAlpha(ch)){
                    scanIdentifier();
                }
                else Jlox.error(line, "Unexpected Character.");
            }
        }
    }
    private boolean isAtEnd(){
        return current >= source.length();
    }

    private char advance(){
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void scanString(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++;
            advance();
        }
        if(isAtEnd()){
            Jlox.error(line, "Unterminated string.");
            return;
        }
        advance();
        String str = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, str);
    }

    private void scanNumber(){
        while (isDigit(peek())) advance();
        if(peek() == '.' && isDigit(peekNext())){
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void scanIdentifier(){
        while(isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
    }

    private boolean match(char expected){
        if(isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }


    private boolean isDigit(char ch){
        return ch >= '0' && ch <= '9';
    }


    private char peekNext(){
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char ch){
        return (ch >= 'a' && ch <= 'z') ||
                (ch >= 'A' && ch <= 'Z') ||
                ch == '_';
    }

    private boolean isAlphaNumeric(char ch){
        return isAlpha(ch) || isDigit(ch);
    }
}
