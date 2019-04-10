package com.codingchili.instance.model.items;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

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
