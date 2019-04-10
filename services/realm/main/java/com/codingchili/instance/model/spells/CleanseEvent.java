package com.codingchili.instance.model.spells;

import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;
import com.codingchili.instance.model.stats.Stats;

import java.util.List;

/**
 * @author Robin Duda
 */
public class CleanseEvent implements Event {
    private List<String> cleansed;
    private Stats stats;
    private String targetId;

    public CleanseEvent(Creature creature, List<String> cleansed) {
        this.stats = creature.getStats();
        this.targetId = creature.getId();
        this.cleansed = cleansed;
    }

    public List<String> getCleansed() {
        return cleansed;
    }

    public Stats getStats() {
        return stats;
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public EventType getRoute() {
        return EventType.cleanse;
    }
}
