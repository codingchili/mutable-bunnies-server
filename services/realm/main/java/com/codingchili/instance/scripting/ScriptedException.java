package com.codingchili.instance.scripting;

import javax.script.ScriptException;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * Wraps a ScriptException as a CoreRuntimeException.
 */
public class ScriptedException extends CoreRuntimeException {

    /**
     * Wraps a JSR233 exception in a runtime exception that includes line and column
     * number together with the exception message.
     *
     * @param e the original exception message.
     */
    public ScriptedException(ScriptException e) {
        super(String.format("Script error: '%s' on line %d:%d.",
                e.getMessage(), e.getLineNumber(), e.getColumnNumber()));
    }
}
