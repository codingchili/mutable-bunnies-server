package com.codingchili.instance.model.items;

import com.codingchili.instance.model.npc.DB;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 */
public class ItemDB {
    private static final String CONF_PATH = "conf/game/item";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static DB<Item> items;

    public ItemDB(CoreContext core) {
        if (!initialized.getAndSet(true)) {
            items = new DB<>(core, Item.class, CONF_PATH);
        }
    }

    public Optional<Item> getById(String id) {
        return items.getById(id);
    }
}
