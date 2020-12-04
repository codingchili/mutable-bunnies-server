package com.codingchili.instance.model.npc;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.codingchili.core.context.CoreContext;
import com.codingchili.instance.context.CachedResponse;
import com.codingchili.instance.model.MetadataStore;
import com.codingchili.instance.model.events.EventType;
import io.vertx.core.buffer.Buffer;

/**
 * Database of all NPC configured.
 */
public class NpcDB implements MetadataStore<EntityConfig> {
    private static final String CONF_PATH = "conf/game/npc";
    private static DB<EntityConfig> npcs;
    private static Buffer cache;

    /**
     * Creates a new or re-uses an existing npc database.
     *
     * @param core the core context to create the database on.
     */
    public NpcDB(CoreContext core) {
        npcs = DB.create(core, EntityConfig.class, CONF_PATH);
        npcs.setOnInvalidate(this::evict);
    }

    public Optional<EntityConfig> getById(String id) {
        return npcs.getById(id);
    }

    @Override
    public Map<String, EntityConfig> asMap() {
        return npcs.asMap();
    }

    @Override
    public void evict() {
        npcs.asMap().values().forEach(config -> config.setType(EntityType.npc));
        cache = CachedResponse.make(EventType.npc_registry.name(), asMap().values());
    }

    @Override
    public Buffer toBuffer() {
        return cache;
    }

    public Collection<EntityConfig> all() {
        return npcs.asMap().values();
    }
}
