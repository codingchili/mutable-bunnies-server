package com.codingchili.instance.model.skills;

import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

import java.util.Collection;

public class SkillStateEvent implements Event {
    private Collection<SkillProgress> skills;

    public SkillStateEvent(PlayerCreature player) {
        this.skills = player.getSkills().asList();
    }

    public Collection<SkillProgress> getSkills() {
        return skills;
    }

    public void setSkills(Collection<SkillProgress> skills) {
        this.skills = skills;
    }

    @Override
    public EventType getRoute() {
        return EventType.skill_state;
    }
}
