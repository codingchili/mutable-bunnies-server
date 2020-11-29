package com.codingchili.instance.model.skills;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SkillState {
    private Map<SkillType, LearnedSkill> skills = new HashMap<>();

    public Map<SkillType, LearnedSkill> getSkills() {
        return skills;
    }

    public void setSkills(Map<SkillType, LearnedSkill> skills) {
        this.skills = skills;
    }

    @JsonIgnore
    public boolean learned(SkillType type) {
        return skills.containsKey(type);
    }

    @JsonIgnore
    public int level(SkillType type) {
        if (learned(type)) {
            return skills.get(type).getLevel();
        } else {
            return 0;
        }
    }

    public LearnedSkill get(SkillType mining) {
        return skills.get(mining);
    }
}
