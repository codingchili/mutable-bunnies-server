package com.codingchili.instance.model.items;

import com.codingchili.instance.model.npc.DB;

import java.util.Optional;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 */
public class ItemDB {
    private static final String CONF_PATH = "conf/game/item";
    private static DB<Item> items;

    public ItemDB(CoreContext core) {
        items = DB.create(core, Item.class, CONF_PATH);
    }

    public Optional<Item> getById(String id) {
        return items.getById(id);
    }
}
