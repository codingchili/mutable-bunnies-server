package com.codingchili.instance.model.items;

import com.codingchili.instance.model.events.*;
import com.codingchili.instance.model.spells.SpellTarget;

/**
 * @author Robin Duda
 */
public class UseItemEvent implements Event {
    private SpellTarget itemTarget;
    private String itemId;

    @Override
    public Broadcast getBroadcast() {
        return Broadcast.NONE;
    }

    @Override
    public EventType getRoute() {
        return EventType.use_item;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public SpellTarget getItemTarget() {
        return itemTarget;
    }

    public void setItemTarget(SpellTarget itemTarget) {
        this.itemTarget = itemTarget;
    }
}
