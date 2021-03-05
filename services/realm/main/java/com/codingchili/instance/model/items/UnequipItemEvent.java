package com.codingchili.instance.model.items;

import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

/**
 * @author Robin Duda
 */
public class UnequipItemEvent implements Event {
    private final String targetId;
    private final Slot slot;

    public UnequipItemEvent(Creature source, Slot slot) {
        this.slot = slot;
        this.targetId = source.getId();
    }

    public Slot getSlot() {
        return slot;
    }

    @Override
    public EventType getRoute() {
        return EventType.unequip_item;
    }

    @Override
    public String getSource() {
        return targetId;
    }
}
