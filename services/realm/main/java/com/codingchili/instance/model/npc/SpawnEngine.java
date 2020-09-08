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

/**
 * @author Robin Duda
 * <p>
 * Handles NPC stuff.
 */
public class SpawnEngine {
    private static final String DESCRIPTION = "description";
    private static final double TPS = 4; // limits NPCs to 4 AI update per second.
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

    /**
     * Spawns an entity or creature if exists with the given id.
     *
     * @param id the id of the entity to spawn as defined in configuration.
     * @param x  the x point.
     * @param y  the y point.
     * @return the structure or npc that was spawned if any.
     */
    public Optional<? extends Entity> spawn(String id, float x, float y) {
        Optional<EntityConfig> config = npcs.getById(id);

        if (config.isPresent()) {
            return npc(id, x, y);
        } else {
            config = entities.getById(id);
            if (config.isPresent()) {
                return structure(id, x, y);
            } else {
                return Optional.empty();
            }
        }
    }

    /**
     * @param id the id of the npc creature to spawn.
     * @param x  the x point.
     * @param y  the y point.
     * @return the creature that was spawned.
     */
    public Optional<Creature> npc(String id, float x, float y) {
        Optional<EntityConfig> config = npcs.getById(id);

        if (config.isPresent()) {
            Npc npc = new Npc(config.get());

            npc.setBaseStats(config.get().getStats().copy());
            setup(config.get(), npc);
            at(npc, x, y);
            npc.compute();

            game.add(npc);
            return Optional.of(npc);
        } else {
            game.getLogger(getClass()).onError(new NoSuchNpcException(id));
            return Optional.empty();
        }
    }

    /**
     * @param id the id of the npc creature to spawn.
     * @param x  the x point.
     * @param y  the y point.
     * @return the structure that was spawned.
     */
    public Optional<Entity> structure(String id, float x, float y) {
        Optional<EntityConfig> config = entities.getById(id);

        if (config.isPresent()) {
            Structure structure = new Structure(config.get());

            setup(config.get(), structure);
            at(structure, x, y);

            game.add(structure);
            return Optional.of(structure);
        } else {
            game.getLogger(getClass()).onError(new NoSuchNpcException(id));
            return Optional.empty();
        }
    }

    private void setup(EntityConfig config, SimpleEntity entity) {
        entity.setName(config.getName());
        entity.setModel(config.getModel());

        Bindings bindings = new Bindings()
                .setSource(entity)
                .setState(new HashMap<>())
                .set("log", (Consumer<String>) (line) -> {
                    game.getLogger(Npc.class)
                            .log(line);
                }).setContext(game);

        if (config.getSpawn() != null) {
            config.getSpawn().apply(bindings);
        }

        if (config.getTick() != null) {
            game.ticker((ticker) -> {
                if (game.exists(entity.getId())) {
                    config.getTick().apply(bindings);
                } else {
                    ticker.disable();
                }
            }, GameContext.secondsToTicks(TPS));
        }

        if (config.getDialog() != null) {
            game.dialogs().register(entity, config.getDialog());
        }
        entity.getAttributes().put(DESCRIPTION, config.getDescription());
    }

    private void at(Entity entity, float x, float y) {
        entity.getVector()
                .setX(x)
                .setY(y);
    }

    /**
     * Adds the entity to the current context if found in configuration
     * and also permanently adds it to the instance spawn configuration.
     *
     * @param designer
     */
    public Promise<Void> add(DesignerRequest designer) {
        Promise<Void> promise = Promise.promise();
        InstanceSettings settings = game.instance().settings();

        settings.getStructures()
                .add(designer.toSpawnConfig());

        // needs both game ID and template ID to remove.
        // how do we know which SpawnConfig an NPC spawned from?

        executor.executeBlocking((done) -> {
            settings.save();

            spawn(designer.getId(), designer.getX(), designer.getY());

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
