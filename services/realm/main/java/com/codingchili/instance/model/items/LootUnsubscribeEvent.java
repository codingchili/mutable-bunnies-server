package com.codingchili.instance.model.items;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

/**
 * @author Robin Duda
 */
public class LootUnsubscribeEvent implements Event {
    private String entityId;

    public String setSubscribed(String subscribed) {
        return subscribed;
    }

    public String getEntityId() {
        return entityId;
    }

    @Override
    public EventType getRoute() {
        return EventType.loot_unsubscribe;
    }
}
