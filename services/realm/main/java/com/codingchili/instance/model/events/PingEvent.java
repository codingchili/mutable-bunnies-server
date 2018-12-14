package com.codingchili.instance.model.events;

/**
 * @author Robin Duda
 * <p>
 * Emitted to clients - requires a response.
 * Used to check that clients are still connected.
 */
public class PingEvent implements Event {

    @Override
    public EventType getRoute() {
        return EventType.any;
    }
}
