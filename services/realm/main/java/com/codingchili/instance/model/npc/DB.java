package com.codingchili.instance.model.npc;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
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
    private Map<String, E> items;
    private Logger logger;

    public DB(CoreContext core, Class<E> type, String path) {
        this.logger = core.logger(getClass());
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
                    }
                }).build();

        logger.event(DB_LOAD)
                .put(ID_COUNT, items.size())
                .put(ID_NAME, type.getSimpleName()).send(LOADED);
    }

    public Optional<E> getById(String id) {
        E npc = items.get(id);

        if (npc != null) {
            return Optional.of(npc);
        } else {
            return Optional.empty();
        }
    }
}
