package com.codingchili.instance.model.npc;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.Storable;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 */
@SuppressWarnings("unchecked")
public class DB<E extends Storable> {
    private static final String DB_LOAD = "db.load";
    private static final String UPDATED = "updated";
    private static final String LOADED = "loaded";
    private static Map<String, DB<?>> maps = new ConcurrentHashMap<>();
    private Map<String, E> items;
    private Runnable onInvalidate = () -> {};
    private Logger logger;

    /**
     * Creates a new database to hold references of the given class. If the database is already
     * initialized it will be reused.
     *
     * @param core the core context to run file modify listener on.
     * @param type the type of classes to be stored in the database.
     * @param path the path to the files to load initially.
     * @param <T>  the type of the created DB.
     * @return a cached or new DB instance.
     */
    public static <T extends Storable> DB<T> create(CoreContext core, Class<T> type, String path) {
        // make sure to differentiate between types loaded from different paths.
        return (DB<T>) maps.computeIfAbsent(type.getSimpleName() + path, (key) -> new DB<>(core, type, path));
    }

    private DB(CoreContext core, Class<E> type, String path) {
        this.logger = core.logger(type);
        this.items = ConfigurationFactory.readDirectory(path).stream()
                .map(config -> Serializer.unpack(config, type))
                .collect(Collectors.toMap(Storable::getId, (v) -> v));

        FileWatcher.builder(core)
                .onDirectory(path)
                .rate(() -> 1500)
                .withListener(new FileStoreListener() {
                    @Override
                    public void onFileModify(Path modified) {
                        logger.event(DB_LOAD)
                                .put(ID_NAME, type.getSimpleName())
                                .send(UPDATED);

                        E item = Serializer.unpack(
                                ConfigurationFactory.readObject(modified.toString()), type);

                        items.put(item.getId(), item);
                        onInvalidate.run();
                    }
                }).build();

        logger.event(DB_LOAD)
                .put(ID_COUNT, items.size()).send(LOADED);

        onInvalidate.run();
    }

    /**
     * @param listener invoked whenever the DB is loaded or modified.
     */
    public void setOnInvalidate(Runnable listener) {
        this.onInvalidate = listener;

        // assume the DB is dirty when the listener is set because the constructor have finished here.
        listener.run();
    }

    /**
     * @return a reference to the DB items map.
     */
    public Map<String, E> asMap() {
        return items;
    }

    /**
     * @param id the ID of the item to retrieve.
     * @return database object matching the ID if present.
     */
    public Optional<E> getById(String id) {
        E npc = items.get(id);

        if (npc != null) {
            return Optional.of(npc);
        } else {
            return Optional.empty();
        }
    }
}
