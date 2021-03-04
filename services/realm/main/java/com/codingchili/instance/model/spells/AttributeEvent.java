package com.codingchili.instance.model.spells;

import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Entity;
import com.codingchili.instance.model.events.*;

import java.util.Random;

/**
 * @author Robin Duda
 * <p>
 * Event fired when a creature takes damage;
 */
public class AttributeEvent implements Event {
    private static final Random random = new Random();
    private static final float DEFAULT_VARY = 15.0f;
    private Entity target;
    private Entity source;
    private ModifierType type;
    private String effect;
    private boolean critical = false;
    private double value;
    private double current;
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

    public AttributeEvent current(double current) {
        this.current = current;
        return this;
    }

    public double getCurrent() {
        return current;
    }

    public AttributeEvent setValue(double value) {
        this.value = value;
        return this;
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

    public AttributeEvent vary() {
        vary(DEFAULT_VARY);
        return this;
    }

    public AttributeEvent vary(float percent) {
        value += ((random.nextBoolean() ? -1.0 : 1.0) * (random.nextFloat() * (percent / 100.0f)) * value);
        return this;
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
