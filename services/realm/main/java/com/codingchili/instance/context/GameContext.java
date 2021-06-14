package com.codingchili.instance.context;

import com.codahale.metrics.Timer;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.logging.Logger;
import com.codingchili.instance.model.dialog.DialogEngine;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.events.*;
import com.codingchili.instance.model.items.InventoryEngine;
import com.codingchili.instance.model.movement.MovementEngine;
import com.codingchili.instance.model.npc.SpawnEngine;
import com.codingchili.instance.model.questing.QuestEngine;
import com.codingchili.instance.model.skills.SkillEngine;
import com.codingchili.instance.model.spells.SpellEngine;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;
import com.codingchili.realm.model.ClassDB;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Robin Duda
 * <p>
 * The core game loop.
 */
public class GameContext {
    public static final int TICK_INTERVAL_MS = 16;
    private static final String GAMELOOP = "gameloop";
    private final Map<EventType, Map<String, EventProtocol>> listeners = new ConcurrentHashMap<>();
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final Set<Ticker> tickers = new ConcurrentHashSet<>();
    private final AtomicInteger skippedTicks = new AtomicInteger(0);
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final InstanceContext instance;
    private final Grid<Creature> creatures;
    private final Grid<Entity> structures;

    private final SpellEngine spells;
    private final MovementEngine movement;
    private final InventoryEngine inventory;
    private final DialogEngine dialogs;
    private final SpawnEngine npcs;
    private final QuestEngine quests;
    private final SkillEngine skills;

    private final ClassDB classes;
    private final Logger logger;
    private final Timer timer;
    private Long currentTick = 0L;

    public GameContext(InstanceContext instance) {
        this.instance = instance;

        int size = instance.settings().getProjection()
                .getCartesianSize()
                .getHighestAxis();

        this.creatures = new LinkedGrid<>(size);
        this.structures = new LinkedGrid<>(size);
        this.classes = new ClassDB(instance);
        this.logger = instance.logger(getClass());
        this.timer = instance.registry().timer(GAMELOOP);

        ticker(creatures::update, 1);
        ticker(structures::update, 5);

        this.spells = new SpellEngine(this);
        this.inventory = new InventoryEngine(this);
        this.movement = new MovementEngine(this);
        this.dialogs = new DialogEngine(this);
        this.npcs = new SpawnEngine(this);
        this.quests = new QuestEngine(this);
        this.skills = new SkillEngine(this);

        initialize(instance.settings());

        instance.periodic(
                TimerSource.of(TICK_INTERVAL_MS).setName(instance.address()),
                (id) -> timer.time(() -> tick(id))
        );
    }

    private void initialize(InstanceSettings settings) {
        Scripted startup = settings.getOnStartup();

        if (startup != null) {
            startup.apply(new Bindings().setContext(this));
        }
    }

    private void tick(Long id) {
        if (processing.getAndSet(true)) {
            skippedTicks.incrementAndGet();
        } else {
            if (skippedTicks.get() > 0) {
                instance.skippedTicks(skippedTicks.getAndSet(0));
            }

            if (closed.get()) {
                instance.cancel(id);
            } else {

                Runnable runnable;
                while ((runnable = queue.poll()) != null) {
                    runnable.run();
                }

                tickers.forEach(ticker -> {
                    if (ticker.get().equals(ticker.incrementAndGet())) {
                        ticker.run();
                    }
                });

                currentTick++;
                if (currentTick == Long.MAX_VALUE) {
                    currentTick = 0L;
                }
                processing.set(false);
            }
        }
    }


    /**
     * @param runnable queues the runnable asynchronously to be executed in the next update.
     * @return fluent.
     */
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

    public DialogEngine dialogs() {
        return dialogs;
    }

    public MovementEngine movement() {
        return movement;
    }

    public InventoryEngine inventory() {
        return inventory;
    }

    public SpawnEngine spawner() {
        return npcs;
    }

    public QuestEngine quests() {
        return quests;
    }

    public SkillEngine skills() {
        return skills;
    }

    public void close() {
        closed.set(true);
        publish(new ShutdownEvent());
    }

    public Ticker ticker(Consumer<Ticker> runnable, Integer interval) {
        return new Ticker(this, runnable, interval);
    }

    public void setTicker(Ticker ticker) {
        queue(() -> {
            if (ticker.active()) {
                tickers.add(ticker);
            } else {
                tickers.remove(ticker);
            }
        });
    }

    public void add(Entity entity) {
        entity.setContext(this);

        publish(new SpawnEvent(entity));
        subscribe(entity.getId(), entity.protocol());

        if (isCreature(entity)) {
            creatures.add((Creature) entity);
        } else {
            structures.add(entity);
        }
        entity.joined();
    }

    public void remove(Entity entity) {
        if (isCreature(entity)) {
            creatures.remove(entity.getId());
        } else {
            structures.remove(entity.getId());
        }

        unsubscribe(entity.getId());
        publish(new DespawnEvent(entity));

        entity.removed();
    }

    private boolean isCreature(Entity entity) {
        return entity instanceof Creature;
    }

    private void unsubscribe(String subscriberId) {
        listeners.forEach((key, value) -> value.remove(subscriberId));
    }

    public EventProtocol subscribe(String subscriberId, EventProtocol protocol) {
        protocol.available().stream()
                .map(EventType::valueOf)
                .forEach(event -> {
                    listeners.computeIfAbsent(event, (key) -> new ConcurrentHashMap<>());
                    listeners.get(event).put(subscriberId, protocol);
                });
        return protocol;
    }

    public void publish(Event event) {
        Map<String, EventProtocol> scoped = listeners.computeIfAbsent(event.getRoute(), (key) -> new ConcurrentHashMap<>());

        String type = event.getRoute().toString();

        switch (event.getBroadcast()) {
            case PARTITION:
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

    public InstanceContext instance() {
        return instance;
    }

    public static Integer onAllTicks() {
        return 1;
    }

    public static Integer secondsToTicks(double seconds) {
        return (int) (seconds * 1000 / TICK_INTERVAL_MS);
    }

    public static Integer secondsToMs(Float seconds) {
        return (int) (seconds * 1000);
    }

    public static float msToSeconds(long ms) {
        return ms / 1000.0f;
    }
}