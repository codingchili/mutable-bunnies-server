package com.codingchili.instance.model.skills;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

import java.util.Map;

public class SkillDetailsEvent implements Event {
    private Map<String, SkillConfig> skills;

    public SkillDetailsEvent(Map<String, SkillConfig> skills) {
        this.skills = skills;
    }

    public Map<String, SkillConfig> getSkills() {
        return skills;
    }

    public void setSkills(Map<String, SkillConfig> skills) {
        this.skills = skills;
    }

    @Override
    public EventType getRoute() {
        return EventType.skill_info;
    }
}
