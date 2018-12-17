package com.codingchili.instance.scripting;

import javax.script.*;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * Provides script support for groovy - requires groovy jsr on classpath.
 */
public class GroovyScript implements Scripted {
    private static final ScriptEngineManager factory = new ScriptEngineManager();
    private static final ScriptEngine engine = factory.getEngineByName("groovy");
    public static final String TYPE = "groovy";
    private CompiledScript compiled;
    private String source;

    public GroovyScript(String source) {
        try {
            this.source = source;
            this.compiled = ((Compilable) engine).compile(source);
        } catch (ScriptException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T apply(Bindings bindings) {
        javax.script.Bindings bound = engine.createBindings();

        if (bindings != null) {
            bindings.forEach(bound::put);
        }
        try {
            return (T) compiled.eval(bound);
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
