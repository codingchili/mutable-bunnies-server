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
public class EventLog implements Receiver<Event> {
    private EventProtocol protocol = new EventProtocol(this);
    private Map<String, List<SpellLogEntry>> logs = new HashMap<>();
    private GameContext game;
    private Logger logger;

    public EventLog(GameContext game) {
        game.subscribe(UUID.randomUUID().toString(), protocol);
        logger = game.getLogger(getClass());
    }

    @Api
    public void spawn(SpawnEvent spawn) {
        logs.remove(spawn.getSource());
    }

    @Api
    public void despawn(DespawnEvent spawn) {
        logs.remove(spawn.getSource());
    }

    @Api
    public void death(DeathEvent event) {
        logs.remove(event.getSource());
    }

    @Api
    public void attribute(AttributeEvent event) {
        //event.getEffect();
        //event.getSourceId();
        //event.getType()
    }

    @Override
    public void handle(Event event) {
        protocol.get(event.getRoute().name()).submit(event);
    }
}
