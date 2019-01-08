package com.codingchili.instance.model.npc;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;

import java.util.*;

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
     * @param npcs a list of npcs to spawn.
     */
    public List<Creature> spawn(String... npcs) {
        List<Creature> result = new ArrayList<>(npcs.length);

        for (String npc: npcs) {
            spawn(npc).ifPresent(result::add);
        }
        return result;
    }

    /**
     * @param id the id of the npc creature to spawn.
     */
    public Optional<Creature> spawn(String id) {
        Optional<NpcConfiguration> configuration = npcs.getById(id);

        if (configuration.isPresent()) {
            Npc npc = new Npc()
                    .setConfiguration(configuration.get());

            game.add(npc);
            return Optional.of(npc);
        } else {
            game.getLogger(getClass()).onError(new NoSuchNpcException(id));
            return Optional.empty();
        }
    }
}
