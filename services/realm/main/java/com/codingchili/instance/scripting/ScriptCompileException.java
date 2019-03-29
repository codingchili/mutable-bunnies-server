package com.codingchili.instance.scripting;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * Thrown on compilation failure.
 */
public class ScriptCompileException extends CoreRuntimeException {

    /**
     * @param fileName the file that was being compiled.
     * @param original the original exception with a compilation error.
     */
    public ScriptCompileException(String fileName, Throwable original) {
        super(String.format("Failed to compile '%s' %s", fileName, original.getMessage()));
    }
}
