package com.codingchili.instance.model.items;

import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

/**
 * @author Robin Duda
 */
public class InventoryUpdateEvent implements Event {
    private Creature source;

    public InventoryUpdateEvent(Creature source) {
        this.source = source;
    }

    public Inventory getInventory() {
        return source.getInventory();
    }

    @Override
    public String getSource() {
        return source.getId();
    }

    @Override
    public EventType getRoute() {
        return EventType.inventory_update;
    }
}
