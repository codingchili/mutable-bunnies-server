package com.codingchili.instance.model.events;

/**
 * @author Robin Duda
 */
public class EquipItemEvent implements Event {
    private String source;
    private String itemId;

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
        return null;
    }

    public EquipItemEvent setSource(String source) {
        this.source = source;
        return this;
    }

    public String getItemId() {
        return itemId;
    }

    public EquipItemEvent setItemId(String itemId) {
        this.itemId = itemId;
        return this;
    }
}
