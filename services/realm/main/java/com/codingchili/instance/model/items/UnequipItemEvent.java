package com.codingchili.instance.model.items;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

/**
 * @author Robin Duda
 */
public class UnequipItemEvent implements Event {
    private Slot slot;

    @Override
    public EventType getRoute() {
        return EventType.unequip_item;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }
}
