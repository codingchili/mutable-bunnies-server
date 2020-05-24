package com.codingchili.instance.model.spells;

import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Entity;
import com.codingchili.instance.model.events.*;

/**
 * @author Robin Duda
 * <p>
 * Event fired when a creature takes damage;
 */
public class AttributeEvent implements Event {
    private Entity target;
    private Entity source;
    private ModifierType type;
    private String effect;
    private boolean critical = false;
    private double value;
    private Runnable completer;

    public AttributeEvent(Creature source, Creature target) {
        this.source = source;
        this.target = target;
    }

    public AttributeEvent completer(Runnable completer) {
        this.completer = completer;
        return this;
    }

    public void apply() {
        completer.run();
    }

    public AttributeEvent target(Creature target) {
        this.target = target;
        return this;
    }

    public AttributeEvent source(Creature source) {
        this.source = source;
        return this;
    }

    public AttributeEvent energy(Double value) {
        this.type = ModifierType.energy;
        this.value = value;
        return this;
    }

    public AttributeEvent heal(Double value) {
        this.type = ModifierType.heal;
        this.value = value;
        return this;
    }

    public AttributeEvent poison(Double value) {
        this.type = ModifierType.poison;
        this.value = value;
        return this;
    }

    public AttributeEvent magical(Double value) {
        this.type = ModifierType.magical;
        this.value = value;
        return this;
    }

    public AttributeEvent physical(Double value) {
        this.type = ModifierType.physical;
        this.value = value;
        return this;
    }

    public AttributeEvent effect(String effect) {
        this.effect = effect;
        return this;
    }

    public AttributeEvent setSource(Creature source) {
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

    public AttributeEvent critical(Boolean is) {
        this.critical = is;
        return this;
    }

    public AttributeEvent normal() {
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

    public ModifierType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    @Override
    public EventType getRoute() {
        return EventType.attribute;
    }

    @Override
    public Broadcast getBroadcast() {
        return Broadcast.PARTITION;
    }
}
