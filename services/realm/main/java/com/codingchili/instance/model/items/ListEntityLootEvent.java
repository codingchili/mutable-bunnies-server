package com.codingchili.instance.model.items;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

import java.util.List;

/**
 * @author Robin Duda
 */
public class ListEntityLootEvent implements Event {
    private List<Item> lootList;
    private String targetId;

    @Override
    public EventType getRoute() {
        return EventType.loot_list;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setLootList(List<Item> lootList) {
        this.lootList = lootList;
    }

    public List<Item> getLootList() {
        return lootList;
    }
}
