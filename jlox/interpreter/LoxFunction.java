package jlox.interpreter;

import jlox.env.Environment;
import jlox.error.Return;
import jlox.parser.Stmt;

import java.util.List;

public class LoxFunction implements LoxCallable{

    private final Stmt.Function declaration;
    private final Environment closure;

    private final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.isInitializer = isInitializer;
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int getArity() {
        return declaration.getParams().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

        for(int i = 0; i < declaration.getParams().size(); i++){
            String param = declaration.getParams().get(i).getLexeme();
            environment.define(param, arguments.get(i));
        }

        try{
            interpreter.executeBlock(declaration.getBody(), environment);
        }
        catch (Return ReturnException){
            if(isInitializer) return closure.getLocal(0, "this");
            return ReturnException.getValue();
        }

        if(isInitializer) return closure.getLocal(0, "this");
        return null;
    }

    @Override
    public String toString() {
        return "<Function> " + declaration.getName().getLexeme();
    }

    LoxFunction bind(LoxInstance instance){
        Environment env = new Environment(closure);
        env.define("this", instance);
        return new LoxFunction(declaration, env, isInitializer);
    }
}
