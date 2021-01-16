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

    private ChatEvent() {}

    public ChatEvent(Entity source, String text) {
        this.source = source.getId();
        this.text = text;
    }

    public static ChatEvent system(String text) {
        return new ChatEvent()
                .setSystem(true)
                .setText(text);
    }

    public ChatEvent setText(String text) {
        this.text = text;
        return this;
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
