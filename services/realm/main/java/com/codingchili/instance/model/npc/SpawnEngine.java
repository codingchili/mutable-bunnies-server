package com.codingchili.instance.model.npc;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.InstanceSettings;
import com.codingchili.instance.model.designer.DesignerRequest;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.scripting.Bindings;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.codingchili.instance.model.entity.Interaction.DESCRIPTION;

/**
 * @author Robin Duda
 * <p>
 * Handles NPC stuff.
 */
public class SpawnEngine {
    private static final String LOG = "log";
    private static final double INTERVAL = 2; // seconds between AI updates.
    private static final int POOL_SIZE = 1;
    private static final int MAX_EXECUTE_TIME = 2;
    private static final String EXECUTOR_NAME = "instance-writer";
    private WorkerExecutor executor;
    private GameContext game;
    private EntityDB entities;
    private NpcDB npcs;

    /**
     * @param game creates a new npc engine on the given context.
     */
    public SpawnEngine(GameContext game) {
        this.game = game;
        this.entities = new EntityDB(game.instance());
        this.npcs = new NpcDB(game.instance());

        // shared across all jvm-local instances.
        executor = game.instance().
                vertx().createSharedWorkerExecutor(EXECUTOR_NAME,
                POOL_SIZE,
                MAX_EXECUTE_TIME,
                TimeUnit.SECONDS
        );
    }

    public NpcDB npcs() {
        return npcs;
    }

    public EntityDB entities() {
        return entities;
    }

    public <T extends Entity> Optional<T> spawn(String id, float x, float y) {
        return spawn(new SpawnConfig().setId(id).setPoint(new Point((int) x, (int) y)));
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> Optional<T> spawn(SpawnConfig spawn) {
        Optional<EntityConfig> config = npcs.getById(spawn.getId());

        if (config.isPresent()) {
            return Optional.of((T) npc(spawn, config.get()));
        } else {
            config = entities.getById(spawn.getId());
            if (config.isPresent()) {
                return Optional.of((T) structure(spawn, config.get()));
            } else {
                game.getLogger(getClass()).onError(new NoSuchNpcException(spawn.getId()));
                return Optional.empty();
            }
        }
    }

    private Creature npc(SpawnConfig spawn, EntityConfig config) {
        Npc npc = new Npc(config);
        npc.setBaseStats(config.getStats().copy());
        npc.compute();
        setup(config, npc, spawn);
        at(npc, spawn.getPoint());
        game.add(npc);
        return npc;
    }

    public Structure structure(SpawnConfig spawn, EntityConfig config) {
        Structure structure = new Structure(config);
        setup(config, structure, spawn);
        at(structure, spawn.getPoint());
        game.add(structure);
        return structure;
    }

    private <T> void applyIfSet(Supplier<T> optional, Consumer<T> setter) {
        Optional.ofNullable(optional.get()).ifPresent(setter);
    }

    private void registerInteractions(EntityConfig config, SimpleEntity entity) {
        applyIfSet(config::getHarvest, harvest -> game.skills().register(entity, harvest));
        applyIfSet(config::getDialog, dialog -> game.dialogs().register(entity, dialog));
    }

    private void setup(EntityConfig config, SimpleEntity entity, SpawnConfig spawn) {
        entity.setName(config.getName());
        entity.setModel(config.getModel().copy().apply(spawn));
        registerInteractions(config, entity);
        Bindings bindings = getBindings(entity);

        applyIfSet(config::getSpawn, it -> it.apply(bindings));
        applyIfSet(config::getTick, tick -> {
            game.ticker((ticker) -> {
                if (game.exists(entity.getId())) {
                    tick.apply(bindings);
                } else {
                    ticker.disable();
                }
            }, GameContext.secondsToTicks(INTERVAL)).offset();
        });
        entity.getAttributes().put(DESCRIPTION, config.getDescription());
    }

    private Bindings getBindings(Entity entity) {
        return new Bindings()
                .setSource(entity)
                .setState(new HashMap<>())
                .set(LOG, (Consumer<String>) (line) ->
                        game.getLogger(Npc.class).log(line)).setContext(game);
    }

    private void at(Entity entity, Point point) {
        entity.getVector()
                .setX(point.getX())
                .setY(point.getY());
    }

    /**
     * Adds the entity to the current context if found in configuration
     * and also permanently adds it to the instance spawn configuration.
     *
     * @param config
     */
    public Promise<Void> add(DesignerRequest config) {
        Promise<Void> promise = Promise.promise();
        InstanceSettings settings = game.instance().settings();

        settings.getStructures().add(config);

        executor.executeBlocking((done) -> {
            settings.save();

            spawn(config);

            done.complete();
        }, false, done -> {
            if (done.failed()) {
                game.getLogger(getClass())
                        .onError(done.cause());
            }
        });
        return promise;
    }

    /**
     * Removes the entity from the current context if present and
     * removes it from configuration.
     *
     * @param designer
     */
    public void remove(DesignerRequest designer) {

    }
}
