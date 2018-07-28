package com.codingchili.realm.instance.model.events;

/**
 * @author Robin Duda
 */
public class DropItemEvent implements Event {
    private String itemId;

    @Override
    public EventType getRoute() {
        return EventType.drop_item;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
