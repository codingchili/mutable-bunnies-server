package com.codingchili.realm.instance.model.events;

/**
 * @author Robin Duda
 */
public class LootEntityEvent implements Event {
    private String targetId;
    private String itemId;

    @Override
    public EventType getRoute() {
        return EventType.loot_item;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
