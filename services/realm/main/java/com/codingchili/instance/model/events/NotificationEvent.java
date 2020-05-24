package com.codingchili.instance.model.events;

import static com.codingchili.instance.model.events.EventType.notification;

public class NotificationEvent implements Event {
    private String message;

    public NotificationEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public EventType getRoute() {
        return notification;
    }
}
