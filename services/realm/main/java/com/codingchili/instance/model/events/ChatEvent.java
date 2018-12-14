package com.codingchili.instance.model.events;

import com.codingchili.instance.model.entity.Entity;

/**
 * @author Robin Duda
 */
public class ChatEvent implements Event {
    private String source;
    private String text;

    public ChatEvent(Entity source, String text) {
        this.source = source.getId();
        this.text = text;
    }

    public String getText() {
        return text;
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
