package com.codingchili.instance.model.afflictions;

import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Random;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 */
public class Affliction implements Storable {
    private static transient Random random = new Random();
    private String id = "no_id";
    private String name = "missing name";
    private String description = "missing description";
    private Float duration = 8.0f;
    private Float interval = 1.0f;
    private Float chance = 1.0f;
    private Scripted modifier;
    private Scripted tick;
    private boolean persist;

    public ActiveAffliction apply(Creature source, Creature target) {
        return new ActiveAffliction(source, target, this);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPersist() {
        return persist;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public <T> T tick(Bindings bindings) {
        if (tick != null && interval > 0) {
            if (chance == 1.0f || random.nextFloat() < chance) {
                return tick.apply(bindings);
            }
        }
        return null;
    }

    public <T> T apply(Bindings bindings) {
        if (modifier != null) {
            return modifier.apply(bindings);
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getDuration() {
        return duration;
    }

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    public Float getChance() {
        return chance;
    }

    public void setChance(Float chance) {
        this.chance = chance;
    }

    @JsonIgnore
    public Scripted getModifier() {
        return modifier;
    }

    @JsonProperty("modifier")
    public void setModifier(Scripted modifier) {
        this.modifier = modifier;
    }

    @JsonIgnore
    public Scripted getTick() {
        return tick;
    }

    @JsonProperty("tick")
    public void setTick(Scripted tick) {
        this.tick = tick;
    }

    public Float getInterval() {
        return interval;
    }

    public void setInterval(Float interval) {
        this.interval = interval;
    }
}
