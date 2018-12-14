package com.codingchili.instance.scripting;

import javax.script.*;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * Provides scripting support for javascript.
 */
public class JavaScript implements Scripted {
    private static final ScriptEngineManager factory = new ScriptEngineManager();
    public static final String TYPE = "js";
    private static final ScriptEngine engine = factory.getEngineByName("javascript");
    private CompiledScript compiled;
    private String source;

    public JavaScript(String source) {
        this.source = source;
        try {
            this.compiled = ((Compilable) engine).compile(source);
        } catch (ScriptException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T apply(Bindings bindings) {
        javax.script.Bindings bind = engine.createBindings();

        if (bindings != null) {
            bindings.forEach(bind::put);
        }
        try {
            T result = (T) compiled.eval(bind);

            // Nashorn really likes doubles - we use floats for now.
            if (result instanceof Double) {
                result = (T) (Object)(((Double) result).floatValue());
            }

            return result;
        } catch (ScriptException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    @Override
    public String getEngine() {
        return TYPE;
    }

    @Override
    public String getSource() {
        return source;
    }
}
