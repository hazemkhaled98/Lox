package jlox.interpreter;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable{

    private final String name;
    private final LoxClass superClass;
    private final Map<String, LoxFunction> methods;

    public LoxClass(String name, LoxClass superClass, Map<String, LoxFunction> methods) {
        this.name = name;
        this.superClass = superClass;
        this.methods = methods;
    }

    public String getName() {
        return name;
    }


    public String toString(){
        return "<Class> " + name;
    }

    @Override
    public int getArity() {
        LoxFunction initializer = findMethod("init");
        return initializer != null ? initializer.getArity() : 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance =  new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if(initializer != null)
            initializer.bind(instance).call(interpreter, arguments);
        return instance;
    }

    LoxFunction findMethod(String name){
        if(methods.containsKey(name))
            return methods.get(name);
        if(superClass != null)
            return superClass.findMethod(name);
        return null;
    }
}
