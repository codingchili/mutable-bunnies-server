package com.codingchili.realm.model;

import com.codingchili.instance.context.CachedResponse;
import com.codingchili.instance.model.MetadataStore;
import com.codingchili.instance.model.entity.PlayableClass;
import com.codingchili.instance.model.npc.DB;
import io.vertx.core.buffer.Buffer;

import java.util.Map;
import java.util.Optional;

import com.codingchili.core.context.CoreContext;

import static com.codingchili.common.Strings.PATH_GAME_CLASSES;

/**
 * @author Robin Duda
 * <p>
 * Contains a list of playable classes.
 */
public class ClassDB implements MetadataStore<PlayableClass> {
    private DB<PlayableClass> classes;
    private Buffer cache;

    public ClassDB(CoreContext core) {
        this.classes = DB.create(core, PlayableClass.class, PATH_GAME_CLASSES);
        this.classes.setOnInvalidate(this::evict);
    }

    @Override
    public Optional<PlayableClass> getById(String id) {
        if (isEnabled(id)) {
            return classes.getById(id);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Map<String, PlayableClass> asMap() {
        return classes.asMap();
    }

    /**
     * @param name the name of the class to check if enabled.
     * @return true if the class is configured, false otherwise.
     */
    public boolean isEnabled(String name) {
        return classes.asMap().containsKey(name);
    }


    @Override
    public void evict() {
        cache = CachedResponse.make( "classinfo", classes.asMap().values());
    }

    @Override
    public Buffer toBuffer() {
        return cache;
    }
}
