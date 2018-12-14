package com.codingchili.instance.transport;

import com.codingchili.instance.model.events.Event;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * @author Robin Duda
 *
 * An update message from the realm or instance to a single client.
 */
public class UpdateMessage implements ReceivableMessage {
    @JsonUnwrapped
    private Event event;
    @JsonProperty
    private String target;

    public UpdateMessage(Event event, String target) {
        this.event = event;
        this.target = target;
    }

    public Event event() {
        return event;
    }

    @Override
    public String target() {
        return target;
    }

    @Override
    public String route() {
        return event.getRoute().name();
    }
}
