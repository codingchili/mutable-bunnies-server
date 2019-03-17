package com.codingchili.instance.model.npc;

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
    private EntityConfiguration config;

    public Npc(EntityConfiguration config) {
        this.config = config;
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

    public Npc setConfiguration(EntityConfiguration config) {
        this.config = config;
        return this;
    }
}
