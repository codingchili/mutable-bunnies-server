package com.codingchili.instance.model.events;

/**
 * Error event emitted to client on error.
 */
public class ErrorEvent implements Event {
    private String message;

    public ErrorEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public EventType getRoute() {
        return null;
    }
}
