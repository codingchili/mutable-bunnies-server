package com.codingchili.instance.model.items;

import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.events.*;

/**
 * @author Robin Duda
 */
public class EquipItemEvent implements Event {
    private String source;
    private Item item;
    private Slot slot;

    public EquipItemEvent(Creature source, Item item, Slot slot) {
        this.item = item;
        this.source = source.getId();
        this.slot = slot;
    }

    public Slot getSlot() {
        return slot;
    }

    public Item getItem() {
        return item;
    }

    public EquipItemEvent setItem(Item item) {
        this.item = item;
        return this;
    }

    public EquipItemEvent setSource(String source) {
        this.source = source;
        return this;
    }

    @Override
    public Broadcast getBroadcast() {
        return Broadcast.PARTITION;
    }

    @Override
    public EventType getRoute() {
        return EventType.equip_item;
    }

    @Override
    public String getSource() {
        return source;
    }
}
