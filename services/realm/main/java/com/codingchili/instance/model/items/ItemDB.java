package com.codingchili.instance.model.items;

import com.codingchili.instance.model.npc.DB;

import java.util.Optional;
import java.util.UUID;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.protocol.Serializer;

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
        return Serializer.kryo(kryo -> {
            Optional<Item> item = items.getById(id);

            if (item.isPresent()) {
                // create a copy of the item to prevent multiple inventories pointing
                // to the same inventory item.
                Item copy = kryo.copy(item.get());
                copy.setId(UUID.randomUUID().toString());
                return Optional.of(copy);
            } else {
                return item;
            }
        });
    }
}
