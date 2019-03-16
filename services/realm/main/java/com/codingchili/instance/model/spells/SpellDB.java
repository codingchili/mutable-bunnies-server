package com.codingchili.instance.model.spells;

import com.codingchili.instance.context.CachedResponse;
import com.codingchili.instance.model.MetadataStore;
import io.vertx.core.buffer.Buffer;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.CoreStrings.ID_COUNT;

/**
 * @author Robin Duda
 * <p>
 * Container of all registered spells.
 */
public class SpellDB implements MetadataStore<Spell> {
    private static final String CONF_PATH = "conf/game/spells/";
    private static final String SPELL_LOAD = "spell.load";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static Map<String, Spell> spells = new HashMap<>();
    private static Buffer cache;
    private Logger logger;

    /**
     * @param core the context to run the DB on.
     */
    public SpellDB(CoreContext core) {
        this.logger = core.logger(getClass());

        if (!initialized.getAndSet(true)) {
            spells = ConfigurationFactory.readDirectory(CONF_PATH).stream()
                    .map(config -> Serializer.unpack(config, Spell.class))
                    .collect(Collectors.toMap(Spell::getId, (v) -> v));

            logger.event(SPELL_LOAD).put(ID_COUNT, spells.size()).send();

            FileWatcher.builder(core)
                    .onDirectory(CONF_PATH)
                    .rate(() -> 1500)
                    .withListener(new FileStoreListener() {
                        @Override
                        public void onFileModify(Path path) {
                            logger.event(SPELL_LOAD).send("spell updated " + path.toString());
                            Spell spell = Serializer.unpack(
                                    ConfigurationFactory.readObject(path.toString()), Spell.class);
                            spells.put(spell.getId(), spell);
                            evict();
                        }
                    }).build();
        }
        evict();
    }

    @Override
    public Optional<Spell> getByName(String name) {
        return Optional.ofNullable(spells.get(name));
    }

    @Override
    public Map<String, Spell> asMap() {
        return spells;
    }

    @Override
    public void evict() {
        cache = CachedResponse.make("spellinfo", spells.values());
    }

    @Override
    public Buffer toBuffer() {
        return cache;
    }
}
