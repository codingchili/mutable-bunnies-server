package com.codingchili.instance.model.npc;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.scripting.Bindings;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Robin Duda
 * <p>
 * Handles NPC stuff.
 */
public class SpawnEngine {
    private static final String DESCRIPTION = "description";
    private static final double TPS = 4; // limits NPCs to 4 AI update per second.
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
    }

    /**
     * @param id the id of the npc creature to spawn.
     * @param x  the x point.
     * @param y  the y point.
     * @return the creature that was spawned.
     */
    public Optional<Creature> npc(String id, float x, float y) {
        Optional<EntityConfiguration> config = npcs.getById(id);

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
        Optional<EntityConfiguration> config = entities.getById(id);

        if (config.isPresent()) {
            Structure structure = new Structure(config.get());

            setup(config.get(), structure);
            at(structure, x, y);

            game.add(structure);
            return Optional.of(structure);
        } else {
            return Optional.empty();
        }
    }

    private void setup(EntityConfiguration config, SimpleEntity entity) {
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
}
