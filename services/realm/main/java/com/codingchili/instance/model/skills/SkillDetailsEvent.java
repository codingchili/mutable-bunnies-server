package com.codingchili.instance.model.skills;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

public class SkillDetailsEvent implements Event {
    private SkillConfig skill;

    public SkillDetailsEvent(SkillConfig skill) {
        this.skill = skill;
    }

    public SkillConfig getSkill() {
        return skill;
    }

    public void setSkill(SkillConfig skill) {
        this.skill = skill;
    }

    @Override
    public EventType getRoute() {
        return EventType.skill_info;
    }
}
