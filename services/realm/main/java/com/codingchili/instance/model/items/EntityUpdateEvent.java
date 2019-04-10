package com.codingchili.instance.model.items;

import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

/**
 * @author Robin Duda
 */
public class EntityUpdateEvent implements Event {
    private Creature updated;

    public EntityUpdateEvent(Creature updated) {
        this.updated = updated;
    }

    public Creature getUpdated() {
        return updated;
    }

    @Override
    public EventType getRoute() {
        return EventType.update;
    }
}
