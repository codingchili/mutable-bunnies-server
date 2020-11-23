package com.codingchili.instance.model.events;

import com.codingchili.instance.model.entity.Entity;

/**
 * @author Robin Duda
 */
public class DespawnEvent implements Event {
    private String id;

    public DespawnEvent(Entity entity) {
        this.id = entity.getId();
    }

    public String getEntityId() {
        return id;
    }

    @Override
    public EventType getRoute() {
        return EventType.despawn;
    }
}
