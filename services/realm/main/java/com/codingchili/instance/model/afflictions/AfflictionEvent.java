package com.codingchili.instance.model.afflictions;

import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;
import com.codingchili.instance.model.stats.Stats;

/**
 * @author Robin Duda
 */
public class AfflictionEvent implements Event {
    private ActiveAffliction active;
    private Stats stats;

    public AfflictionEvent(Creature target, ActiveAffliction affliction) {
        this.active = affliction;
        this.stats = target.getStats();
    }

    public Stats getStats() {
        return stats;
    }

    public String getSourceId() {
        return active.getSourceId();
    }

    public String getTargetId() {
        return active.getTargetId();
    }

    public Float getDuration() {
        return active.getAffliction().getDuration();
    }

    public Affliction getAffliction() {
        return active.getAffliction();
    }

    @Override
    public EventType getRoute() {
        return EventType.affliction;
    }
}
