package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.stats.Stats;

/**
 * @author Robin Duda
 *
 * Update event for creature stats.
 */
public class StatsUpdateEvent implements Event {
    private String targetId;
    private Stats stats;


    public StatsUpdateEvent(Creature creature) {
        this.targetId = creature.getId();
        this.stats = creature.getStats();
    }

    public String getTargetId() {
        return targetId;
    }

    public Stats getStats() {
        return stats;
    }

    @Override
    public EventType getRoute() {
        return EventType.stats;
    }

    @Override
    public Broadcast getBroadcast() {
        return Broadcast.PARTITION;
    }
}
