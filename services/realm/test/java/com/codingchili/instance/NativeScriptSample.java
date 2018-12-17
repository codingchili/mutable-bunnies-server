package com.codingchili.instance;

import com.codingchili.instance.scripting.Bindings;

import java.util.function.Function;

/**
 * @author Robin Duda
 *
 * Representation of a native script.
 */
public class NativeScriptSample<T> implements Function<Bindings, T> {

    @SuppressWarnings("unchecked")
    @Override
    public T apply(Bindings bindings) {
        return (T) (Object) (1817.71 * (Float) bindings.get("number"));
    }
}
