package com.codingchili.instance.model.events;

import com.codingchili.instance.model.entity.Entity;

/**
 * @author Robin Duda
 */
public class SpawnEvent implements Event {
    private Entity entity;

    public SpawnEvent(Entity entity) {
        this.entity = entity;
    }

    public SpawnEvent setEntity(Entity entity) {
        this.entity = entity;
        return this;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public EventType getRoute() {
        return EventType.spawn;
    }
}
