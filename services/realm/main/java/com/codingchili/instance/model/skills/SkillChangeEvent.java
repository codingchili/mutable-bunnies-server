package com.codingchili.instance.model.skills;

import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

public class SkillChangeEvent implements Event {
    private SkillProgress skill;
    private Boolean levelup = false;
    private Boolean learned = false;
    private int experience;

    public SkillChangeEvent(PlayerCreature player, SkillType type) {
        this.skill = player.getSkills().getSkills().get(type);
    }

    public SkillChangeEvent(SkillProgress skill) {
        this.skill = skill;
    }

    public SkillProgress getSkill() {
        return skill;
    }

    public SkillChangeEvent setSkill(SkillProgress skill) {
        this.skill = skill;
        return this;
    }

    public Boolean getLearned() {
        return learned;
    }

    public SkillChangeEvent setLearned(Boolean learned) {
        this.learned = learned;
        return this;
    }

    public Boolean getLevelup() {
        return levelup;
    }

    public SkillChangeEvent setLevelup(Boolean levelup) {
        this.levelup = levelup;
        return this;
    }

    public int getExperience() {
        return experience;
    }

    public SkillChangeEvent setExperience(int experience) {
        this.experience = experience;
        return this;
    }

    @Override
    public EventType getRoute() {
        return EventType.skill_change;
    }
}
