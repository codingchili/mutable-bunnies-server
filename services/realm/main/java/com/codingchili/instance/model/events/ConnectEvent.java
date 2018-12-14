package com.codingchili.instance.model.events;

import com.codingchili.instance.model.entity.Entity;
import com.codingchili.instance.model.entity.PlayerCreature;

import java.util.List;

/**
 * @author Robin Duda
 *
 * On connection this event is emitted to the connecting player.
 * It contains all entities in the current instance.
 */
public class ConnectEvent implements Event {
    private PlayerCreature creature;
    private List<Entity> entities;

    public ConnectEvent(PlayerCreature creature, List<Entity> entities) {
        this.entities = entities;
        this.creature = creature;
    }

    @Override
    public String getSource() {
        return creature.getId();
    }

    @Override
    public EventType getRoute() {
        return EventType.join;
    }

    public SpawnEvent.SpawnType getSpawn() {
        return SpawnEvent.SpawnType.SPAWN;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }
}
