package com.codingchili.realm.instance.context;

import com.codingchili.realm.instance.model.entity.*;
import com.codingchili.realm.instance.model.events.*;
import com.codingchili.realm.instance.model.spells.MovementEngine;
import com.codingchili.realm.instance.model.spells.SpellEngine;
import com.codingchili.realm.model.ClassDB;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.codingchili.core.logging.Logger;

import static com.codingchili.realm.instance.model.events.SpawnEvent.SpawnType.DESPAWN;

/**
 * @author Robin Duda
 * <p>
 * The core game loop.
 */
public class GameContext {
    private static final int TICK_INTERVAL_MS = 16;
    private Map<EventType, Map<String, EventProtocol>> listeners = new ConcurrentHashMap<>();
    private Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private Set<Ticker> tickers = new ConcurrentHashSet<>();
    private AtomicInteger skippedTicks = new AtomicInteger(0);
    private AtomicBoolean processing = new AtomicBoolean(false);
    private AtomicBoolean closed = new AtomicBoolean(false);
    private InstanceContext instance;
    private Grid<Creature> creatures;
    private Grid<Entity> structures;
    private SpellEngine spells;
    private MovementEngine movement;
    private ClassDB classes;
    private Long currentTick = 0L;

    public GameContext(InstanceContext instance) {
        this.instance = instance;

        int width = instance.settings().getWidth();
        this.creatures = new Grid<>(width);
        this.structures = new Grid<>(width);
        this.classes = new ClassDB(instance);

        ticker(creatures::update, 1);
        ticker(structures::update, 5);

        this.spells = new SpellEngine(this);
        this.movement = new MovementEngine(this);

        instance.periodic(() -> TICK_INTERVAL_MS, instance.address(), this::tick);
    }

    private void tick(Long timer) {
        if (processing.getAndSet(true)) {
            skippedTicks.incrementAndGet();
        } else {

            if (skippedTicks.get() > 0) {
                instance.skippedTicks(skippedTicks.getAndSet(0));
            }

            instance.blocking(block -> {
                Runnable runnable;

                while ((runnable = queue.poll()) != null) {
                    runnable.run();
                }

                tickers.forEach(ticker -> {
                    if (currentTick % ticker.get() == 0) {
                        ticker.run();
                    }
                });

                block.complete();
            }, (done) -> {
                if (done.succeeded()) {
                    if (closed.get()) {
                        instance.cancel(timer);
                    } else {
                        currentTick++;
                        if (currentTick == Long.MAX_VALUE) {
                            currentTick = 0L;
                        }
                    }
                } else {
                    done.cause().printStackTrace();
                }
                processing.set(false);
            });
        }
    }

    public GameContext queue(Runnable runnable) {
        queue.add(runnable);
        return this;
    }

    public ClassDB classes() {
        return this.classes;
    }

    public Grid<Creature> creatures() {
        return creatures;
    }

    public Grid<Entity> entities() {
        return structures;
    }

    public SpellEngine spells() {
        return spells;
    }

    public MovementEngine movement() {
        return movement;
    }

    public void close() {
        closed.set(true);
        publish(new ShutdownEvent());
    }

    public Ticker ticker(Consumer<Ticker> runnable, Integer interval) {
        return new Ticker(this, runnable, interval);
    }

    public void setTicker(Ticker ticker) {
        if (ticker.get() > 0) {
            tickers.add(ticker);
        } else {
            tickers.remove(ticker);
        }
    }

    public void add(Creature creature) {
        addNew(creature);
        creatures.add(creature);
    }

    public void add(Entity entity) {
        addNew(entity);
        structures.add(entity);
    }

    private void addNew(Entity entity) {
        entity.setContext(this);
        publish(new SpawnEvent().setEntities(entity));
        subscribe(entity);
    }

    public void remove(Entity entity) {
        creatures.remove(entity.getId());
        structures.remove(entity.getId());
        unsubscribe(entity);
        publish(new SpawnEvent().setEntities(entity).setType(DESPAWN));
    }

    private void unsubscribe(Entity entity) {
        listeners.forEach((key, value) -> value.remove(entity.getId()));
    }

    public EventProtocol subscribe(Entity entity) {
        EventProtocol protocol = entity.protocol();

        protocol.available().stream()
                .map(EventType::valueOf)
                .forEach(event -> {
                    listeners.computeIfAbsent(event, (key) -> new ConcurrentHashMap<>());
                    listeners.get(event).put(entity.getId(), protocol);
                });
        return protocol;
    }

    public void publish(Event event) {
        Map<String, EventProtocol> scoped = listeners.computeIfAbsent(event.getRoute(), (key) -> new ConcurrentHashMap<>());

        String type = event.getRoute().toString();

        // todo: bulk updates, ClientReceivable with multiple recipient: realm server can encode once per bulk.

        switch (event.getBroadcast()) {
            case PARTITION:
                // todo implement network partitioning.
            case GLOBAL:
                scoped.values().forEach(listener -> listener.get(type).submit(event));
                break;
            case ADJACENT:
                Stream.of(creatures, structures).forEach(grid -> {
                    grid.adjacent(getById(event.getSource()).getVector()).forEach(entity -> {
                        scoped.get(entity.getId()).get(type).submit(event);
                    });
                });
                break;
        }
    }

    public boolean exists(String id) {
        return (creatures.exists(id) || structures.exists(id));
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T getById(String id) {
        Entity entity = null;
        if (creatures.exists(id)) {
            entity = creatures.get(id);
        } else {
            if (structures.exists(id)) {
                entity = structures.get(id);
            }
        }
        Objects.requireNonNull(entity, String.format("Could not find entity with id '%s'.", id));
        return (T) entity;
    }

    public Logger getLogger(Class<?> aClass) {
        return instance.logger(aClass);
    }

    public InstanceContext getInstance() {
        return instance;
    }

    public static Integer secondsToTicks(double seconds) {
        return (int) (seconds * 1000 / TICK_INTERVAL_MS);
    }

    public static double ticksToSeconds(int ticks) {
        return (ticks * TICK_INTERVAL_MS) / 1000;
    }
}