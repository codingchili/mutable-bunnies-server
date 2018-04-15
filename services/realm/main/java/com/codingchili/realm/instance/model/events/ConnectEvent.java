package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.PlayerCreature;

import java.util.List;

/**
 * @author Robin Duda
 *
 * On connection this event is emitted to the connecting player.
 * It contains all entities in the current instance.
 */
public class ConnectEvent implements Event {
    private PlayerCreature creature;
    private List<SpawnEvent> spawn;

    public ConnectEvent(PlayerCreature creature, List<SpawnEvent> spawn) {
        this.spawn = spawn;
        this.creature = creature;
    }

    @Override
    public String getSource() {
        return creature.getId();
    }

    @Override
    public EventType getType() {
        return EventType.SPAWN;
    }

    public List<SpawnEvent> getSpawn() {
        return spawn;
    }

    public void setSpawn(List<SpawnEvent> spawn) {
        this.spawn = spawn;
    }
}
