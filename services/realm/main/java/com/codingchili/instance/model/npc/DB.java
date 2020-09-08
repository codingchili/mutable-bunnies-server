package com.codingchili.instance.model.npc;

import com.codingchili.common.Strings;
import io.vertx.core.json.JsonObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.files.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.Storable;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * A database used to load local configuration files into a typed map which
 * supports reloading objects on the fly.
 */
@SuppressWarnings("unchecked")
public class DB<E extends Storable> {
    private static final int POLL_FILE_MODIFY = 1500;
    private static final String DB_LOAD = "db.load";
    private static final String UPDATED = "updated";
    private static final Map<String, DB<?>> maps = new ConcurrentHashMap<>();
    private final Map<String, E> items;
    private Runnable onInvalidate = () -> { };
    private final Logger logger;

    /**
     * Creates a new database to hold references of the given class. If the database is already
     * initialized it will be reused. The name of the loaded file will be used as it's ID.
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
        long start = System.currentTimeMillis();

        this.logger = core.logger(type);

        // iterate all subdirectories of the root path and set the ID of the loaded
        // object to the file name, example filename "the_file.yaml" -> id "the_file".
        this.items = ConfigurationFactory.enumerate(path, true)
                .map(this::parseWithId)
                .map(config -> Serializer.unpack(config, type))
                .collect(Collectors.toMap(Storable::getId, (v) -> v));

        FileWatcher.builder(core)
                .onDirectory(path)
                .rate(TimerSource.of(POLL_FILE_MODIFY))
                .withListener(new FileStoreListener() {
                    @Override
                    public void onFileModify(Path modified) {
                        E item = Serializer.unpack(parseWithId(modified.toString()), type);

                        logger.event(DB_LOAD)
                                .put(ID_NAME, item.getId())
                                .send(UPDATED);

                        items.put(item.getId(), item);
                        onInvalidate.run();
                    }
                }).build();

        logger.event(DB_LOAD)
                .put(ID_COUNT, items.size())
                .put(ID_TIME, (System.currentTimeMillis() - start) + "ms")
                .send();

        onInvalidate.run();
    }

    private JsonObject parseWithId(String filePath) {
        JsonObject json = ConfigurationFactory.readObject(filePath);

        // allow ID to be overridden through configuration.
        if (!json.containsKey(Strings.ID)) {
            json.put(Strings.ID, getIdFromFileName(filePath));
        }
        return json;
    }

    private static String getIdFromFileName(String filePath) {
        String fileName = Paths.get(filePath).getFileName().toString();
        int extensionAt = fileName.lastIndexOf(".");
        return (extensionAt == -1) ? fileName : fileName.substring(0, extensionAt);
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
        E item = items.get(id);

        if (item != null) {
            return Optional.of(item);
        } else {
            return Optional.empty();
        }
    }
}
