package com.codingchili.instance.model.skills;

import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

public class SkillStateEvent implements Event {
    private SkillState skills;

    public SkillStateEvent(PlayerCreature player) {
        this.skills = player.getSkills();
    }

    public SkillState getSkills() {
        return skills;
    }

    public void setSkills(SkillState skills) {
        this.skills = skills;
    }

    @Override
    public EventType getRoute() {
        return EventType.skill_state;
    }
}
