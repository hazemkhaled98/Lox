package jlox.interpreter;

import jlox.Jlox;
import jlox.env.Environment;
import jlox.error.Return;
import jlox.error.RuntimeError;
import jlox.parser.Expr;
import jlox.parser.Stmt;
import jlox.scanner.Token;
import jlox.scanner.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor {


    private final Environment globals = new Environment();
    private final Map<Expr, Integer> locals = new HashMap<>();
    private Environment env = globals;


    public Interpreter(){
        globals.define("clock", new LoxCallable() {
            @Override
            public int getArity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }
        });

        globals.define("print", new LoxCallable() {
            @Override
            public int getArity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                for (Object argument : arguments)
                    System.out.print(stringify(argument));
                return null;
            }
        });

        globals.define("println", new LoxCallable() {
            @Override
            public int getArity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                for (Object argument : arguments)
                    System.out.println(stringify(argument));
                return null;
            }
        });
    }

    public void interpret(List<Stmt> statements){
        try{
            for(Stmt statement : statements){
                execute(statement);
            }
        }
        catch (RuntimeError error){
            Jlox.runtimeError(error);
        }
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    @Override
    public void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.getExpression());
    }

    @Override
    public void visitVarStmt(Stmt.Var stmt) {
        Object value = null;

        if(stmt.getInitializer() != null){
            value = evaluate(stmt.getInitializer());
        }

        env.define(stmt.getIdentifier().getLexeme(), value);
    }

    @Override
    public void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.getStatements(), new Environment(this.env));
    }

    @Override
    public void visitIfStmt(Stmt.If stmt) {
        if(isTruthy(evaluate(stmt.getCondition())))
            execute(stmt.getThenBranch());
        else if(stmt.getElseBranch() != null)
            execute(stmt.getElseBranch());
    }

    @Override
    public void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.getCondition())))
            execute(stmt.getBody());
    }

    @Override
    public void visitFunStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, env, false);
        env.define(stmt.getName().getLexeme(), function);
    }

    @Override
    public void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;

        if(stmt.getValue() != null)
            value = evaluate(stmt.getValue());

        throw new Return(value);
    }

    @Override
    public void visitClassStmt(Stmt.Class stmt) {
        Object superClass = null;
        if(stmt.getSuperClass() != null){
            superClass = evaluate(stmt.getSuperClass());
            if(!(superClass instanceof LoxClass))
                throw new RuntimeError(stmt.getSuperClass().getName(), "Superclass must be a class.");
        }
        env.define(stmt.getName().getLexeme(), null);

        if(stmt.getSuperClass() != null){
            env = new Environment(env);
            env.define("super", superClass);
        }
        Map<String, LoxFunction> methods = new HashMap<>();
        for(Stmt.Function method : stmt.getMethods()){
            boolean isInitializer = method.getName().getLexeme().equals("init");
            LoxFunction function = new LoxFunction(method, env, isInitializer);
            methods.put(method.getName().getLexeme(), function);
        }
        LoxClass newClass = new LoxClass(stmt.getName().getLexeme(), (LoxClass) superClass,methods);
        if(superClass != null)
            env = env.getEnclosing();
        env.assign(stmt.getName(), newClass);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object leftVal = evaluate(expr.getLeft());
        Object rightVal = evaluate(expr.getRight());
        return switch (expr.getOperator().getType()){
            case PLUS -> {
                if(leftVal instanceof Double && rightVal instanceof Double){
                    yield (Double) leftVal + (Double) rightVal;
                }
                if(leftVal instanceof String && rightVal instanceof String){
                    yield String.valueOf(leftVal) + rightVal;
                }
                if(leftVal instanceof String && rightVal instanceof Double){
                    yield leftVal + stringify(rightVal);
                }
                if(leftVal instanceof Double && rightVal instanceof String){
                    yield stringify(leftVal) + rightVal;
                }
                if(leftVal instanceof Double && rightVal instanceof Boolean){
                    yield (Double) leftVal + ((Boolean) rightVal ? 1 : 0);
                }
                if(leftVal instanceof Boolean && rightVal instanceof Double){
                    yield ((Boolean) leftVal ? 1 : 0) + (Double) rightVal;
                }
                throw new RuntimeError(expr.getOperator(), "Invalid operands.");
            }
            case MINUS -> {
                checkNumOperands(expr.getOperator(), leftVal, rightVal);
                yield (Double) leftVal - (Double) rightVal;
            }
            case STAR -> {
                checkNumOperands(expr.getOperator(), leftVal, rightVal);
                yield (Double) leftVal * (Double) rightVal;
            }
            case SLASH -> {
                checkNumOperands(expr.getOperator(), leftVal, rightVal);
                yield (Double) leftVal / (Double) rightVal;
            }
            case GREATER -> {
                checkNumOperands(expr.getOperator(), leftVal, rightVal);
                yield (Double) leftVal > (Double) rightVal;
            }
            case GREATER_EQUAL -> {
                checkNumOperands(expr.getOperator(), leftVal, rightVal);
                yield (Double) leftVal >= (Double) rightVal;
            }
            case LESS -> {
                checkNumOperands(expr.getOperator(), leftVal, rightVal);
                yield (Double) leftVal < (Double) rightVal;
            }
            case LESS_EQUAL -> {
                checkNumOperands(expr.getOperator(), leftVal, rightVal);
                yield (Double) leftVal <= (Double) rightVal;
            }
            case EQUAL_EQUAL -> isEqual(leftVal, rightVal);
            case BANG_EQUAL -> !isEqual(leftVal, rightVal);
            default -> null;
        };
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.getExpression());
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object rightVal = evaluate(expr.getRight());

        return switch (expr.getOperator().getType()){
            case BANG -> !isTruthy(rightVal);
            case MINUS -> {
                checkNumOperand(expr.getOperator(), rightVal);
                yield -(Double) rightVal;
            }
            default -> null;
        };
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.getName(), expr);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.getValue());
        Integer distance = locals.get(expr);
        if(distance != null)
            env.assignLocal(distance, expr.getName(), value);
        else globals.assign(expr.getName(), value);
        return value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.getLeft());

        if(expr.getOperator().getType() == TokenType.OR){
            if(isTruthy(left))
                return left;
        }
        else {
            if(!isTruthy(left))
                return left;
        }

        return evaluate(expr.getRight());
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.getCallee());

        List<Object> arguments = new ArrayList<>();
        for(Expr argument : expr.getArguments()){
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof LoxCallable function))
            throw new RuntimeError(expr.getParen(), "Invalid callee type: can only call functions and classes.");


        if(arguments.size() != function.getArity())
            throw new RuntimeError(expr.getParen(), "Expected " + function.getArity() + " arguments but got " + arguments.size() + ".");

        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.getObject());
        if (object instanceof LoxInstance instance)
            return instance.get(expr.getName());

        throw new RuntimeError(expr.getName(), "Only instance can have properties.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.getObject());

        if(object instanceof LoxInstance instance){
            Object value = evaluate(expr.getValue());
            instance.set(expr.getName(), value);
            return value;
        }

        throw new RuntimeError(expr.getName(), "Only instances have fields.");
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.getKeyword(), expr);
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        LoxClass superClass = (LoxClass) env.getLocal(distance, "super");
        LoxInstance object = (LoxInstance) env.getLocal(distance - 1, "this");
        LoxFunction method = superClass.findMethod(expr.getMethod().getLexeme());
        if(method == null){
            throw new RuntimeError(expr.getMethod(), "undefined property '" + expr.getMethod().getLexeme() + "'.");
        }
        return method.bind(object);
    }


    void executeBlock(List<Stmt> statements, Environment innerEnv){
        Environment outerEnv = this.env;
        try{
            this.env = innerEnv;
            for (Stmt statement : statements){
                execute(statement);
            }
        }
        finally {
            this.env = outerEnv;
        }
    }

    private void execute(Stmt statement){
        statement.accept(this);
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    private boolean isTruthy(Object val){
        if(val == null)
            return false;
        if(val instanceof Boolean)
            return (Boolean) val;
        if(val instanceof String)
            return !((String) val).isBlank();
        if(val instanceof Double)
            return ((Double) val) != 0.0;
        return true;
    }

    private boolean isEqual(Object first, Object second){
        if(first == null && second == null)
            return true;
        if(first == null)
            return false;
        return first.equals(second);
    }

    private void checkNumOperand(Token operator, Object operand){
        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumOperands(Token operator, Object left, Object right){
        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private String stringify(Object value){
        if (value == null) return "nil";
        if(value instanceof Double){
            String result = String.valueOf(value);
            if(result.endsWith(".0"))
                return result.substring(0, result.length() - 2);
            return result;
        }
        return value.toString();
    }

    private Object lookUpVariable(Token identifier, Expr expr){
        Integer distance = locals.get(expr);

        if(distance != null)
            return env.getLocal(distance, identifier.getLexeme());

        return globals.get(identifier);
    }
}
