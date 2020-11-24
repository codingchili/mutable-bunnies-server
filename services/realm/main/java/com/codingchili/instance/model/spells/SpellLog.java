package com.codingchili.instance.model.spells;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Api;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.EventProtocol;
import com.codingchili.instance.model.events.DespawnEvent;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.SpawnEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Records spell-casts and damage events.
 * <p>
 * Keep in-memory logs for death info.
 * Log to remote for balancing/analysis.
 *
 * todo: filter for creatures only?
 */
public class SpellLog implements Receiver<Event> {
    private EventProtocol protocol = new EventProtocol(this);
    private Map<String, List<SpellLogEntry>> logs = new HashMap<>();
    private Logger logger;
    private GameContext game;

    public SpellLog(GameContext game) {
        game.subscribe(UUID.randomUUID().toString(), protocol);
        logger = game.getLogger(getClass());
    }

    @Api
    public void spawn(SpawnEvent spawn) {
        // clear logs on spawn/despawn.
        //logger.log("something spawned");
        logs.remove(spawn.getSource());
    }

    @Api
    public void despawn(DespawnEvent spawn) {
        // clear logs on spawn/despawn.
        //logger.log("something despawned");
        logs.remove(spawn.getSource());
    }

    @Api
    public void death(DeathEvent event) {
        // record death
        //logger.log("something died");
    }

    @Api
    public void attribute(AttributeEvent event) {
        // record damage, remove old damage events too old. <addeddate, event>
        //logger.log("something damaged");
    }

    @Override
    public void handle(Event event) {
        protocol.get(event.getRoute().name()).submit(event);
    }
}
