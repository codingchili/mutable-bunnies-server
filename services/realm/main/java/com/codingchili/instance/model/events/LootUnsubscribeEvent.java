package com.codingchili.instance.model.events;

/**
 * @author Robin Duda
 */
public class LootUnsubscribeEvent implements Event {
    private String subscribed;

    public String setSubscribed(String subscribed) {
        return subscribed;
    }

    public String getSubscribed() {
        return subscribed;
    }

    @Override
    public EventType getRoute() {
        return EventType.loot_unsubscribe;
    }
}
