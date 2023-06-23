package jlox.resolver;

import jlox.Jlox;
import jlox.interpreter.Interpreter;
import jlox.parser.Expr;
import jlox.parser.Stmt;
import jlox.scanner.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor {

    private enum FunctionType {
        NONE, FUNCTION, INITIALIZER, METHOD
    }

    private enum ClassType{
        NONE, CLASS, SUBCLASS
    }

    private final Interpreter interpreter;
    private final ArrayList<HashMap<String, Boolean>> scopes = new ArrayList<>();

    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolve(List<Stmt> statements){
        for(Stmt statement : statements)
            resolve(statement);
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.getExpression());
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        var scope = !scopes.isEmpty() ? scopes.get(scopes.size() - 1) : null;
        if(scope != null && scope.get(expr.getName().getLexeme()) == Boolean.FALSE)
            Jlox.error(expr.getName(), "Can't read local variable in its own initializer.");

        resolveLocal(expr, expr.getName());
        return null;
    }


    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.getValue());
        resolveLocal(expr, expr.getName());
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.getCallee());

        for(Expr argument : expr.getArguments())
            resolve(argument);

        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.getObject());
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.getValue());
        resolve(expr.getObject());
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if(currentClass != ClassType.CLASS){
            Jlox.error(expr.getKeyword(), "Can't use 'this' outside of a class.");
        }
        resolveLocal(expr, expr.getKeyword());
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if(currentClass == ClassType.NONE)
            Jlox.error(expr.getKeyword(), "Can't use 'super' outside of a class.");
        else if(currentClass != ClassType.SUBCLASS)
            Jlox.error(expr.getKeyword(), "Can't use 'super' in a class with no superclass.");
        resolveLocal(expr, expr.getKeyword());
        return null;
    }

    @Override
    public void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.getExpression());
    }

    @Override
    public void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.getIdentifier());
        if(stmt.getInitializer() != null)
            resolve(stmt.getInitializer());
        define(stmt.getIdentifier());
    }

    @Override
    public void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.getStatements());
        endScope();
    }

    @Override
    public void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.getCondition());
        resolve(stmt.getThenBranch());
        if(stmt.getElseBranch() != null) resolve(stmt.getElseBranch());
    }

    @Override
    public void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.getCondition());
        resolve(stmt.getBody());
    }

    @Override
    public void visitFunStmt(Stmt.Function stmt) {
        declare(stmt.getName());
        define(stmt.getName());

        resolveFunction(stmt, FunctionType.FUNCTION);
    }

    @Override
    public void visitReturnStmt(Stmt.Return stmt) {
        if(currentFunction == FunctionType.NONE)
            Jlox.error(stmt.getKeyword(), "Can't return from top-level code.");
        if(stmt.getValue() != null){
            if (currentFunction == FunctionType.INITIALIZER)
                Jlox.error(stmt.getKeyword(), "Can't return a value from an initializer");
            resolve(stmt.getValue());
        }
    }

    @Override
    public void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.getName());
        define(stmt.getName());

        if(stmt.getSuperClass() != null){
            if(stmt.getName().getLexeme().equals(stmt.getSuperClass().getName().getLexeme()))
                Jlox.error(stmt.getSuperClass().getName(), "A class can't inherit from itself");
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.getSuperClass());
            beginScope();
            scopes.get(scopes.size() - 1).put("super", true);
        }

        beginScope();
        var scope = scopes.get(scopes.size() - 1);
        scope.put("this", true);

        for(Stmt.Function method : stmt.getMethods()){
            FunctionType declaration = FunctionType.METHOD;
            if(method.getName().getLexeme().equals("init"))
                declaration = FunctionType.INITIALIZER;
            resolveFunction(method, declaration);
        }

        endScope();
        if(stmt.getSuperClass() != null) endScope();
        currentClass = enclosingClass;
    }


    private void resolve(Stmt statement){
        statement.accept(this);
    }

    private void resolve(Expr expression){
        expression.accept(this);
    }

    private void beginScope(){
        scopes.add(new HashMap<>());
    }

    private void endScope(){
        scopes.remove(scopes.size() - 1);
    }

    private void declare(Token identifier){
        if(scopes.isEmpty()) return;

        var scope = scopes.get(scopes.size() - 1);
        if(scope.containsKey(identifier.getLexeme()))
            Jlox.error(identifier, "Already defined variable with this name in this scope.");
        scope.put(identifier.getLexeme(), false);
    }

    private void define(Token identifier){
        if(scopes.isEmpty()) return;

        var scope = scopes.get(scopes.size() - 1);
        scope.put(identifier.getLexeme(), true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for(int i = scopes.size() - 1; i >= 0; i--){
            var scope = scopes.get(i);
            if(scope.containsKey(name.getLexeme())){
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function function, FunctionType type){
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.getParams()){
            declare(param);
            define(param);
        }
        resolve(function.getBody());
        endScope();

        currentFunction = enclosingFunction;
    }
}
