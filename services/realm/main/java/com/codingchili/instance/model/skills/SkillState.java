package com.codingchili.instance.model.skills;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

/**
 * Player skill state.
 */
public class SkillState {
    private Map<SkillType, SkillProgress> skills = new HashMap<>();

    public Map<SkillType, SkillProgress> getSkills() {
        return skills;
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

    public SkillProgress get(SkillType type) {
        return skills.computeIfAbsent(type, skill -> new SkillProgress(type));
    }
}
