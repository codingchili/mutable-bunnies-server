package com.codingchili.instance.model.skills;

import com.codingchili.core.storage.Storable;
import com.codingchili.instance.scripting.Scripted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration of a player skill.
 */
public class SkillConfig implements Storable {
    private Set<Perk> perks = new HashSet<>();
    private String id;
    private String name;
    private String description;
    private SkillType type;
    private Scripted effectiveness;
    private Scripted scaling;

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SkillType getType() {
        return type;
    }

    public void setType(SkillType type) {
        this.type = type;
    }

    public Set<Perk> getPerks() {
        return perks;
    }

    public void setPerks(Set<Perk> perks) {
        this.perks = perks;
    }

    @JsonIgnore
    public Scripted getScaling() {
        return scaling;
    }

    @JsonProperty("scaling")
    public void setScaling(Scripted scaling) {
        this.scaling = scaling;
    }

    @JsonIgnore
    public Scripted getEffectiveness() {
        return effectiveness;
    }

    @JsonProperty("effectiveness")
    public void setEffectiveness(Scripted effectiveness) {
        this.effectiveness = effectiveness;
    }
}
