package com.codingchili.instance.model.entity;

import com.codingchili.instance.model.npc.DB;
import com.codingchili.instance.model.npc.EntityConfig;

import java.util.Optional;

import com.codingchili.core.context.CoreContext;

/**
 * A database of all configured entities.
 */
public class EntityDB {
    private static final String CONF_PATH = "conf/game/entities";
    private static DB<EntityConfig> items;

    /**
     * Creates a new or re-uses an existing entity database.
     *
     * @param core the core context to create the database on.
     */
    public EntityDB(CoreContext core) {
        items = DB.create(core, EntityConfig.class, CONF_PATH);
    }

    public Optional<EntityConfig> getById(String id) {
        return items.getById(id);
    }
}