package com.codingchili.instance.model.events;

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
