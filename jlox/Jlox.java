package jlox;

import jlox.error.RuntimeError;
import jlox.interpreter.Interpreter;
import jlox.parser.Parser;
import jlox.parser.Stmt;
import jlox.resolver.Resolver;
import jlox.scanner.Scanner;
import jlox.scanner.Token;
import jlox.scanner.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Jlox {

    private final static Interpreter interpreter = new Interpreter();
    static private boolean hadError = false;
    static private boolean hadRuntimeError = false;
    public static void main(String[] args) throws IOException {
        
        if (args.length > 1) {
          System.out.println("Usage: java Jlox [script]");
          System.exit(64); 
        } else if (args.length == 1) {
          runFile(args[0]);
        } else {
          runPrompt();
        }
    } 

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(hadError) System.exit(65);
        if(hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        while(true){
            if(hadError || hadRuntimeError){
                System.out.print("Invalid input!\n");
                hadError = false;
                hadRuntimeError = false;
            }
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null) break;
            run(line);
        }
    }

    private static void run(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        if(hadError) return;
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if(hadError) return;
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        if(hadError) return;
        interpreter.interpret(statements);
    }

    public static void error(int line, String message){
        report(line, "", message);
    }

    public static void error(Token token, String message){
        if(token.getType() == TokenType.EOF)
            report(token.getLine(), "at end", message);
        else
            report(token.getLine(), "at '" + token.getLexeme() + "'", message);
    }

    public static void runtimeError(RuntimeError error){
        System.err.printf("[line %s]: %s", error.getToken().getLine(), error.getMessage());
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message){
        System.err.printf("[line %d] Error %s: %s", line, where, message);
        hadError = true;
    }
    
}

