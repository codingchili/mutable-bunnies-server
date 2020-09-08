package com.codingchili.instance.model.entity;

import com.codingchili.core.context.CoreContext;
import com.codingchili.instance.context.CachedResponse;
import com.codingchili.instance.model.MetadataStore;
import com.codingchili.instance.model.events.EventType;
import com.codingchili.instance.model.npc.DB;
import com.codingchili.instance.model.npc.EntityConfig;
import io.vertx.core.buffer.Buffer;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * A database of all configured entities.
 */
public class EntityDB implements MetadataStore<EntityConfig> {
    private static final String CONF_PATH = "conf/game/entities";
    private static DB<EntityConfig> items;
    private static Buffer cache;

    /**
     * Creates a new or re-uses an existing entity database.
     *
     * @param core the core context to create the database on.
     */
    public EntityDB(CoreContext core) {
        items = DB.create(core, EntityConfig.class, CONF_PATH);
        items.setOnInvalidate(this::evict);
    }

    public Collection<EntityConfig> all() {
        return items.asMap().values();
    }

    public Optional<EntityConfig> getById(String id) {
        return items.getById(id);
    }

    @Override
    public Map<String, EntityConfig> asMap() {
        return items.asMap();
    }

    @Override
    public void evict() {
        cache = CachedResponse.make(EventType.registry.name(), asMap().values());
    }

    @Override
    public Buffer toBuffer() {
        return cache;
    }
}