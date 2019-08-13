package com.codingchili.instance.model.spells;

import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Entity;
import com.codingchili.instance.model.events.*;

/**
 * @author Robin Duda
 * <p>
 * Event fired when a creature takes damage;
 */
public class DamageEvent implements Event {
    private Entity target;
    private Entity source;
    private DamageType damage;
    private boolean critical = false;
    private double value;

    public DamageEvent(Creature target, double value, DamageType damage) {
        this.target = target;
        this.damage = damage;
        this.value = value;
    }

    public DamageEvent setSource(Creature source) {
        this.source = source;
        return this;
    }

    public String getSourceId() {
        if (source != null) {
            return source.getId();
        } else {
            return null;
        }
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public String getTargetId() {
        return target.getId();
    }

    public DamageType getDamage() {
        return damage;
    }

    public double getValue() {
        return value;
    }

    @Override
    public EventType getRoute() {
        return EventType.damage;
    }

    @Override
    public Broadcast getBroadcast() {
        return Broadcast.PARTITION;
    }
}
