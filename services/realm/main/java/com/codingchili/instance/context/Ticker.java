package com.codingchili.instance.context;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Robin Duda
 *
 * Ticker executes periodically on the game loop.
 */
public class Ticker implements Supplier<Integer> {
    private int id = UUID.randomUUID().hashCode();
    private GameContext context;
    private AtomicInteger interval;
    private boolean active = true;
    private Long lastTick = now();
    private Long deltaMS = 0L;
    private Consumer<Ticker> exec;

    /**
     * @param context the game context the ticker runs on.
     * @param exec the task that will be periodically executed.
     * @param interval the interval in game ticks to run the ticker.
     */
    public Ticker(GameContext context, Consumer<Ticker> exec, Integer interval) {
        this.exec = exec;
        this.context = context;
        this.interval = new AtomicInteger(interval);
        context.setTicker(this);
    }

    private long now() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    public void run() {
        Long currentTick = now();
        deltaMS = currentTick - lastTick;

        if (deltaMS < 0) {
            // prevent overflow caused by low accuracy.
            deltaMS = 0L;
        } else {
            exec.accept(this);
            lastTick = currentTick;
        }
    }

    public Ticker interval(Integer tick) {
        this.interval.set(tick);
        context.setTicker(this);
        return this;
    }

    public Long deltaMS() {
        return deltaMS;
    }

    public float delta() {
        return deltaMS * 0.001f;
    }

    public void enable() {
        active = true;
        context.setTicker(this);
    }

    public void disable() {
        active = false;
        context.setTicker(this);
    }

    public boolean active() {
        return active;
    }

    @Override
    public boolean equals(Object other) {
        return ((Ticker) other).id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public Integer get() {
        return interval.get();
    }
}
