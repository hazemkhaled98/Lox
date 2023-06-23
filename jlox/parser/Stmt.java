package jlox.parser;

import jlox.scanner.Token;

import java.util.List;

public abstract class Stmt {
    public interface Visitor{
        void visitExpressionStmt(Expression stmt);
        void visitVarStmt(Var stmt);
        void visitBlockStmt(Block stmt);
        void visitIfStmt(If stmt);
        void visitWhileStmt(While stmt);
        void visitFunStmt(Function stmt);
        void visitReturnStmt(Return stmt);
        void visitClassStmt(Class stmt);
    }
    public abstract void accept(Visitor visitor);

    public static class Expression extends Stmt{

        @Override
        public void accept(Visitor visitor) {
            visitor.visitExpressionStmt(this);
        }

        private final Expr expression;
        Expression(Expr expression){
            this.expression = expression;
        }

        public Expr getExpression(){
            return expression;
        }
    }


    public static class Var extends Stmt{
        public void accept(Visitor visitor){
            visitor.visitVarStmt(this);
        }

        private final Token identifier;

        private final Expr initializer;

        Var(Token identifier, Expr initializer) {
            this.identifier = identifier;
            this.initializer = initializer;
        }

        public Expr getInitializer() {
            return initializer;
        }

        public Token getIdentifier() {
            return identifier;
        }
    }

    public static class Block extends Stmt{

        @Override
        public void accept(Visitor visitor){
            visitor.visitBlockStmt(this);
        }

        private final List<Stmt> statements;

        public Block(List<Stmt> statements){
            this.statements = statements;
        }

        public List<Stmt> getStatements() {
            return statements;
        }
    }

    public static class If extends Stmt{
        @Override
        public void accept(Visitor visitor) {
            visitor.visitIfStmt(this);
        }


        private final Expr condition;
        private final Stmt thenBranch;
        private final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        public Expr getCondition() {
            return condition;
        }

        public Stmt getThenBranch() {
            return thenBranch;
        }

        public Stmt getElseBranch() {
            return elseBranch;
        }
    }

    public static class While extends Stmt{
        public void accept(Visitor visitor){
            visitor.visitWhileStmt(this);
        }

        private final Expr condition;
        private final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        public Expr getCondition() {
            return condition;
        }

        public Stmt getBody() {
            return body;
        }
    }

    public static class Function extends Stmt{
        @Override
        public void accept(Visitor visitor) {
            visitor.visitFunStmt(this);
        }

        private final Token name;
        private final List<Token> params;
        private final List<Stmt> body;

        public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        public Token getName() {
            return name;
        }

        public List<Token> getParams() {
            return params;
        }

        public List<Stmt> getBody() {
            return body;
        }
    }

    public static class Return extends Stmt{
        @Override
        public void accept(Visitor visitor) {
            visitor.visitReturnStmt(this);
        }

        private final Token keyword;
        private final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        public Token getKeyword() {
            return keyword;
        }

        public Expr getValue() {
            return value;
        }
    }

    public static class Class extends Stmt{
        @Override
        public void accept(Visitor visitor) {
            visitor.visitClassStmt(this);
        }

        private final Token name;
        private final Expr.Variable superClass;
        private final List<Stmt.Function> methods;

        public Class(Token name, Expr.Variable superClass, List<Function> methods) {
            this.name = name;
            this.superClass = superClass;
            this.methods = methods;
        }

        public Token getName() {
            return name;
        }

        public Expr.Variable getSuperClass() {
            return superClass;
        }

        public List<Function> getMethods() {
            return methods;
        }
    }
}
