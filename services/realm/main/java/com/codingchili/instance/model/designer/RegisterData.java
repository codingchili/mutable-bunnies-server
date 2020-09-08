package com.codingchili.instance.model.designer;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;
import com.codingchili.instance.model.npc.EntityConfig;

import java.util.Collection;

public class RegisterData implements Event {
    private Collection<EntityConfig> entries;

    public RegisterData(Collection<EntityConfig> entries) {
        this.entries = entries;
    }

    public Collection<EntityConfig> getEntries() {
        return entries;
    }

    public void setEntries(Collection<EntityConfig> entries) {
        this.entries = entries;
    }

    @Override
    public EventType getRoute() {
        return EventType.registry;
    }
}
