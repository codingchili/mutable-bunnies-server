package com.codingchili.instance.model.npc;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.SimpleCreature;
import com.codingchili.instance.model.events.DeathEvent;
import com.codingchili.instance.scripting.Bindings;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 *
 * Model of an NPC.
 */
public class Npc extends SimpleCreature {
    private static final String DESCRIPTION = "description";
    private static final double TPS = 0.2; // limits NPCs to 5 actions per second.
    private NpcConfiguration config;

    @Override
    public void setContext(GameContext game) {
        super.setContext(game);
        Bindings bindings = new Bindings()
                .setSource(this)
                .setContext(game);

        if (config.getSpawn() != null) {
            config.getSpawn().apply(bindings);
        }

        if (config.getTick() != null) {
            game.ticker((ticker) -> {
                if (game.exists(getId())) {
                    config.getTick().apply(bindings);
                } else {
                    ticker.disable();
                }
            }, GameContext.secondsToTicks(TPS));
        }

        if (config.getDialog() != null) {
            game.dialogs().register(this, config.getDialog());
        }

        attributes.put(DESCRIPTION, config.getDescription());

        vector = config.getPoint();
        setName(config.getName());
        setBaseStats(config.getStats());
    }

    @Api(route = "death")
    public void death(DeathEvent event) {
        // how to subscribe only if recipient = this?
        if (event.getTargetId().equals(getId())) {

            if (config.getDeath() != null) {
                Bindings bindings = new Bindings()
                        .setSource(game.creatures().get(event.getSourceId()))
                        .setTarget(this)
                        .setContext(game);

                // looting + xp gain = script or here?

                config.getDeath().apply(bindings);
            }
        }
    }

    public Npc setConfiguration(NpcConfiguration config) {
        this.config = config;
        return this;
    }
}
