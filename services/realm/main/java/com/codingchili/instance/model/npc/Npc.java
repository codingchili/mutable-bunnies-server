package com.codingchili.instance.model.npc;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.SimpleCreature;
import com.codingchili.instance.model.items.ItemDB;
import com.codingchili.instance.model.spells.DeathEvent;
import com.codingchili.instance.model.stats.Stats;
import com.codingchili.instance.scripting.Bindings;

import java.util.Random;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 *
 * Model of an NPC.
 */
public class Npc extends SimpleCreature {
    private EntityConfig config;

    public Npc(EntityConfig config) {
        this.config = config;
    }

    @Override
    public void setContext(GameContext game) {
        super.setContext(game);

        Random random = new Random();
        ItemDB items = new ItemDB(game.instance());

        config.getLoot().forEach(item -> {
            if (random.nextFloat() < item.getProbability()) {
                items.getById(item.getItem()).ifPresent(loot -> {
                    int count = Math.min(random.nextInt(item.getMax()), item.getMin());

                    loot.setQuantity(count);
                    inventory.add(loot);
                });
            }
        });
    }

    @Override
    protected boolean onClassModifier(Stats calculated) {
        return true;
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

                config.getDeath().apply(bindings);
            }
        }
    }

    public Npc setConfiguration(EntityConfig config) {
        this.config = config;
        return this;
    }
}
