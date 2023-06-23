package jlox.env;

import jlox.error.RuntimeError;
import jlox.scanner.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment(){
        enclosing = null;
    }

    public Environment(Environment enclosing){
        this.enclosing = enclosing;
    }

    public void define(String name, Object value){
        values.put(name, value);
    }

    public void assign(Token identifier, Object value){
        if(values.containsKey(identifier.getLexeme()))
            values.put(identifier.getLexeme(), value);

        else throw new RuntimeError(identifier, "Undefined variable '" + identifier.getLexeme() + "'.");
    }

    public Object get(Token identifier){
        if(values.containsKey(identifier.getLexeme()))
            return values.get(identifier.getLexeme());

        throw new RuntimeError(identifier, "Undefined variable '" + identifier.getLexeme() + "'.");
    }

    public Object getLocal(Integer distance, String name) {
        return getAt(distance).values.get(name);
    }

    public void assignLocal(Integer distance, Token name, Object value) {
        getAt(distance).values.put(name.getLexeme(), value);
    }

    private Environment getAt(int distance){
        Environment env = this;
        for(int i = 0; i < distance; i++){
            env = env.enclosing;
        }
        return env;
    }

    public Environment getEnclosing() {
        return enclosing;
    }
}
