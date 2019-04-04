package com.codingchili.instance.model.events;

import com.codingchili.instance.model.afflictions.ActiveAffliction;
import com.codingchili.instance.model.stats.Stats;

/**
 * @author Robin Duda
 */
public class AfflictionEvent implements Event {
    private ActiveAffliction active;
    private Stats stats;

    public AfflictionEvent(ActiveAffliction affliction) {
        this.active = affliction;
        this.stats = affliction.getSource().getStats();
    }

    public Stats getStats() {
        return stats;
    }

    public String getSourceId() {
        return active.getSource().getId();
    }

    public String getTargetId() {
        return active.getTarget().getId();
    }

    public String getId() {
        return active.getAffliction().getId();
    }

    public String getName() {
        return active.getAffliction().getName();
    }

    public String getDescription() {
        return active.getAffliction().getDescription();
    }

    public Float getDuration() {
        return active.getAffliction().getDuration();
    }

    @Override
    public EventType getRoute() {
        return EventType.affliction;
    }
}
