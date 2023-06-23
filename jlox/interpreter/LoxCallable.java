package jlox.interpreter;

import java.util.List;

public interface LoxCallable {

    int getArity();
    Object call (Interpreter interpreter, List<Object> arguments);
}
