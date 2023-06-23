package jlox.interpreter;

import jlox.error.RuntimeError;
import jlox.scanner.Token;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private final LoxClass loxClass;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass loxClass) {
        this.loxClass = loxClass;
    }

    @Override
    public String toString() {
        return loxClass.getName() + " instance";
    }

    Object get(Token name){
        if(fields.containsKey(name.getLexeme()))
            return fields.get(name.getLexeme());
        LoxFunction method = loxClass.findMethod(name.getLexeme());
        if (method != null) return method.bind(this);
        throw new RuntimeError(name, "Undefined Property '" + name.getLexeme() + "'.");
    }

    void set(Token name, Object value){
        fields.put(name.getLexeme(), value);
    }

}
