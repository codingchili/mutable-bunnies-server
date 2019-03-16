package com.codingchili.instance.model.npc;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;

import java.util.Optional;

/**
 * @author Robin Duda
 * <p>
 * Handles NPC stuff.
 */
public class NpcEngine {
    private GameContext game;
    private NpcDB npcs;

    /**
     * @param game creates a new npc engine on the given context.
     */
    public NpcEngine(GameContext game) {
        this.game = game;
        this.npcs = new NpcDB(game.instance());
    }

    /**
     * @param id the id of the npc creature to spawn.
     * @param x the x point.
     * @param y the y point.
     * @return the creature that was spawned.
     */
    public Optional<Creature> spawn(String id, float x, float y) {
        Optional<NpcConfiguration> configuration = npcs.getById(id);

        if (configuration.isPresent()) {
            Npc npc = new Npc()
                    .setConfiguration(configuration.get());

            npc.getVector()
                    .setX(x)
                    .setY(y);

            game.add(npc);
            return Optional.of(npc);
        } else {
            game.getLogger(getClass()).onError(new NoSuchNpcException(id));
            return Optional.empty();
        }
    }
}
