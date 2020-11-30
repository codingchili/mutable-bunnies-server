package com.codingchili.instance.model.skills;

import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

import java.util.Map;

public class SkillStateEvent implements Event {
    private Map<SkillType, SkillProgress> skills;
    private SkillProgress skill;
    private Boolean levelup;
    private int experience;

    public SkillStateEvent(PlayerCreature player) {
        this.skills = player.getSkills().getSkills();
    }

    public SkillStateEvent(SkillProgress skill) {
        this.skill = skill;
    }

    public Map<SkillType, SkillProgress> getSkills() {
        return skills;
    }

    public void setSkills(Map<SkillType, SkillProgress> skills) {
        this.skills = skills;
    }

    public SkillProgress getSkill() {
        return skill;
    }

    public SkillStateEvent setSkill(SkillProgress skill) {
        this.skill = skill;
        return this;
    }

    public Boolean getLevelup() {
        return levelup;
    }

    public SkillStateEvent setLevelup(Boolean levelup) {
        this.levelup = levelup;
        return this;
    }

    public int getExperience() {
        return experience;
    }

    public SkillStateEvent setExperience(int experience) {
        this.experience = experience;
        return this;
    }

    @Override
    public EventType getRoute() {
        return EventType.skill_state;
    }
}
