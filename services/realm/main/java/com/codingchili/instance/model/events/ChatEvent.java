package com.codingchili.instance.model.events;

import com.codingchili.instance.model.entity.Entity;

/**
 * @author Robin Duda
 */
public class ChatEvent implements Event {
    private boolean party;
    private boolean system;
    private String source;
    private String text;

    public ChatEvent(Entity source, String text) {
        this.source = source.getId();
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setParty(boolean party) {
        this.party = party;
    }

    public ChatEvent setSystem(boolean system) {
        this.system = system;
        return this;
    }

    public boolean isParty() {
        return party;
    }

    public boolean isSystem() {
        return system;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public EventType getRoute() {
        return EventType.chat;
    }
}
