package com.codingchili.instance.model.skills;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

/**
 * Player skill state.
 */
public class SkillState {
    private Map<SkillType, SkillProgress> skills = new HashMap<>();

    public Map<SkillType, SkillProgress> getSkills() {
        return skills;
    }

    public Collection<SkillProgress> asList() {
        return skills.values();
    }

    public void setSkills(Map<SkillType, SkillProgress> skills) {
        this.skills = skills;
    }

    @JsonIgnore
    public int level(SkillType type) {
        if (skills.containsKey(type)) {
            return skills.get(type).getLevel();
        } else {
            return 0;
        }
    }

    @JsonIgnore
    public boolean learned(SkillType type) {
        return skills.containsKey(type);
    }

    public SkillProgress get(SkillType type) {
        if (skills.containsKey(type)) {
            return skills.get(type);
        } else {
            throw new SkillNotLearnedException(type);
        }
    }

    public boolean learned(String skillId) {
        return Arrays.stream(SkillType.values())
                .filter(skill -> skillId.equals(skill.name()))
                .anyMatch(skill -> skills.containsKey(skill));
    }

    public void learn(SkillType type) {
        skills.putIfAbsent(type, new SkillProgress(type));
    }
}
