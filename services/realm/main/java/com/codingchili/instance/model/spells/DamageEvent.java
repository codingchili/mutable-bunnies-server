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
    private DamageType type;
    private String effect;
    private boolean critical = false;
    private double value;
    private Runnable completer;

    public DamageEvent(Creature source, Creature target) {
        this.source = source;
        this.target = target;
    }

    public DamageEvent completer(Runnable completer) {
        this.completer = completer;
        return this;
    }

    public void apply() {
        completer.run();
    }

    public DamageEvent target(Creature target) {
        this.target = target;
        return this;
    }

    public DamageEvent source(Creature source) {
        this.source = source;
        return this;
    }

    public DamageEvent heal(Double value) {
        this.type = DamageType.heal;
        this.value = value;
        return this;
    }

    public DamageEvent poison(Double value) {
        this.type = DamageType.poison;
        this.value = value;
        return this;
    }

    public DamageEvent magical(Double value) {
        this.type = DamageType.magical;
        this.value = value;
        return this;
    }

    public DamageEvent physical(Double value) {
        this.type = DamageType.physical;
        this.value = value;
        return this;
    }

    public DamageEvent effect(String effect) {
        this.effect = effect;
        return this;
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

    public DamageEvent critical(Boolean is) {
        this.critical = is;
        return this;
    }

    public DamageEvent normal() {
        this.critical = false;
        return this;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getEffect() {
        return effect;
    }

    public String getTargetId() {
        return target.getId();
    }

    public DamageType getType() {
        return type;
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
