package com.codingchili.instance.model.skills;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Learned when leveling up a skill.
 */
public class Perk {
    // perks may apply to only the given classes.
    private Set<String> classes = new HashSet<>();
    private String id = UUID.randomUUID().toString();
    private String description;
    private String icon;
    private int level;
    private Float effectiveness;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Float getEffectiveness() {
        return effectiveness;
    }

    public void setEffectiveness(Float effectiveness) {
        this.effectiveness = effectiveness;
    }

    public Set<String> getClasses() {
        return classes;
    }

    public void setClasses(Set<String> classes) {
        this.classes = classes;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
