package com.codingchili.realm.model;

import com.codingchili.instance.context.CachedResponse;
import com.codingchili.instance.model.MetadataStore;
import com.codingchili.instance.model.entity.PlayableClass;
import io.vertx.core.buffer.Buffer;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.common.Strings.PATH_GAME_CLASSES;
import static com.codingchili.core.configuration.CoreStrings.ID_COUNT;

/**
 * @author Robin Duda
 * <p>
 * Contains a list of playable classes.
 */
public class ClassDB implements MetadataStore<PlayableClass> {
    private static final String CLASS_LOAD = "classed.load";
    private static Map<String, PlayableClass> classes = new HashMap<>();
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private Buffer cache;
    private Logger logger;

    public ClassDB(CoreContext core) {
        logger = core.logger(getClass());

        if (!initialized.getAndSet(true)) {
            classes = ConfigurationFactory.readDirectory(PATH_GAME_CLASSES).stream()
                    .map(config -> Serializer.unpack(config, PlayableClass.class))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(PlayableClass::getName, (v) -> v));

            logger.event(CLASS_LOAD).put(ID_COUNT, classes.size()).send();

            FileWatcher.builder(core)
                    .onDirectory(PATH_GAME_CLASSES)
                    .rate(() -> 1500)
                    .withListener(new FileStoreListener() {
                        @Override
                        public void onFileModify(Path path) {
                            logger.event(CLASS_LOAD).send("class updated: " + path.toString());

                            PlayableClass playable = Serializer.unpack(
                                    ConfigurationFactory.readObject(path.toString()), PlayableClass.class);

                            classes.put(playable.getName(), playable);
                            evict();
                        }
                    }).build();
        }
        evict();
    }

    @Override
    public Optional<PlayableClass> getByName(String name) {
        if (isEnabled(name)) {
            return Optional.of(classes.get(name));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Map<String, PlayableClass> asMap() {
        return classes;
    }

    /**
     * @param name the name of the class to check if enabled.
     * @return true if the class is configured, false otherwise.
     */
    public boolean isEnabled(String name) {
        return classes.containsKey(name);
    }


    @Override
    public void evict() {
        cache = CachedResponse.make( "classinfo", classes.values());
    }

    @Override
    public Buffer toBuffer() {
        return cache;
    }
}
