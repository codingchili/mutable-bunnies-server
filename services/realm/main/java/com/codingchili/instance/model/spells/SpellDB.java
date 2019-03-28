package com.codingchili.instance.model.spells;

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
 * Container of all registered spells.
 */
public class SpellDB implements MetadataStore<Spell> {
    private static final String CONF_PATH = "conf/game/spells/";
    private static Buffer cache;
    private DB<Spell> spells;

    /**
     * @param core the context to run the DB on.
     */
    public SpellDB(CoreContext core) {
        this.spells = DB.create(core, Spell.class, CONF_PATH);
        this.spells.setOnInvalidate(this::evict);
    }

    @Override
    public Optional<Spell> getByName(String name) {
        return spells.getById(name);
    }

    @Override
    public Map<String, Spell> asMap() {
        return spells.asMap();
    }

    @Override
    public void evict() {
        cache = CachedResponse.make("spellinfo", spells.asMap().values());
    }

    @Override
    public Buffer toBuffer() {
        return cache;
    }
}
