package com.codingchili.instance.model.skills;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

public class SkillExpEvent implements Event {
    @Override
    public EventType getRoute() {
        return EventType.skill_exp;
    }
}
