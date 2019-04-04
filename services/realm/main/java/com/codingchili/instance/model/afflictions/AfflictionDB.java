package com.codingchili.instance.model.afflictions;

import com.codingchili.instance.context.CachedResponse;
import com.codingchili.instance.model.MetadataStore;
import com.codingchili.instance.model.npc.DB;
import io.vertx.core.buffer.Buffer;

import java.util.Map;
import java.util.Optional;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 * <p>
 * Container of all afflictions.
 */
public class AfflictionDB implements MetadataStore<Affliction> {
    private static final String CONF_PATH = "conf/game/afflictions";
    private static Buffer cache;
    private DB<Affliction> db;

    /**
     * @param core the game context that is associated with this instance.
     */
    public AfflictionDB(CoreContext core) {
        this.db = DB.create(core, Affliction.class, CONF_PATH);
        this.db.setOnInvalidate(this::evict);
    }

    @Override
    public Optional<Affliction> getById(String name) {
        return db.getById(name);
    }

    @Override
    public Map<String, Affliction> asMap() {
        return db.asMap();
    }

    @Override
    public void evict() {
        cache = CachedResponse.make("afflictioninfo", db.asMap().values());
    }

    @Override
    public Buffer toBuffer() {
        return cache;
    }
}
