package com.codingchili.instance.model.entity;

import com.codingchili.instance.model.npc.DB;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 */
public class EntityDB {
    private static final String CONF_PATH = "conf/game/entity";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static DB<Entity> items;

    public EntityDB(CoreContext core) {
        if (!initialized.getAndSet(true)) {
            // tbd: use concrete class.
            items = new DB<>(core, Entity.class, CONF_PATH);
        }
    }

    public Optional<Entity> getById(String id) {
        return items.getById(id);
    }
}