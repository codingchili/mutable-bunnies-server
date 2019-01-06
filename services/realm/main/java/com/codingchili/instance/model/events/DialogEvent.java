package com.codingchili.instance.model.events;

import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 */
public class DialogEvent implements Event {
    private Request request;
    private String source;

    public DialogEvent setSource(String sourceId) {
        this.source = sourceId;
        return this;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public EventType getRoute() {
        return EventType.dialog;
    }

    public Request getRequest() {
        return request;
    }

    public DialogEvent setRequest(Request request) {
        this.request = request;
        return this;
    }
}
