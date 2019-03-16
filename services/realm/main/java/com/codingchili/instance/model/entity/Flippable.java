package com.codingchili.instance.model.entity;

import java.util.function.Supplier;

/**
 * @author Robin Duda
 */
public class Flippable<T> {
    private T reference;
    private T a;
    private T b;

    public Flippable(Supplier<T> constructor) {
        this(constructor.get(), constructor.get());
    }

    public Flippable(T a, T b) {
        this.a = a;
        this.b = b;
        this.reference = a;
    }

    public synchronized T writable() {
        return reference;
    }

    public synchronized T readable() {
        return (reference == a) ? b : a;
    }

    public synchronized void flip() {
        reference = (reference == a) ? b : a;
    }
}
