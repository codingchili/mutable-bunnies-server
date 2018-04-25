package com.codingchili.realm.instance.model.afflictions;

import com.codingchili.realm.instance.context.CachedResponse;
import com.codingchili.realm.instance.model.MetadataStore;
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
 * Container of all afflictions.
 */
public class AfflictionDB implements MetadataStore<Affliction> {
    private static final String CONF_PATH = "conf/game/afflictions";
    private static final String AFFLICTION_LOAD = "affliction.load";
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static Map<String, Affliction> afflictions = new HashMap<>();
    private static Buffer cache;
    private Logger logger;

    /**
     * @param core the game context that is associated with this instance.
     */
    public AfflictionDB(CoreContext core) {
        this.logger = core.logger(getClass());

        if (!initialized.getAndSet(true)) {
            afflictions = ConfigurationFactory.readDirectory(CONF_PATH).stream()
                    .map(config -> Serializer.unpack(config, Affliction.class))
                    .filter(Objects::nonNull)
                    .filter(affliction -> affliction.name != null)
                    .collect(Collectors.toMap((k) -> k.name, (v) -> v));

            logger.event(AFFLICTION_LOAD).put(ID_COUNT, afflictions.size()).send();

            FileWatcher.builder(core)
                    .onDirectory(CONF_PATH)
                    .rate(() -> 1500)
                    .withListener(new FileStoreListener() {
                        @Override
                        public void onFileModify(Path path) {
                            logger.event(AFFLICTION_LOAD).send("affliction updated: " + path.toString());
                            Affliction affliction = Serializer.unpack(
                                    ConfigurationFactory.readObject(path.toString()), Affliction.class);

                            afflictions.put(affliction.getName(), affliction);
                            evict();
                        }
                    }).build();
        }
        evict();
    }

    @Override
    public Optional<Affliction> getByName(String name) {
        return Optional.ofNullable(afflictions.get(name));
    }

    @Override
    public Map<String, Affliction> asMap() {
        return afflictions;
    }

    @Override
    public void evict() {
        cache = CachedResponse.make("afflictioninfo", afflictions.values());
    }

    @Override
    public Buffer toBuffer() {
        return cache;
    }
}
