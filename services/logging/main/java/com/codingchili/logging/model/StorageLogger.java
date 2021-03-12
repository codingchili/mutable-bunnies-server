package com.codingchili.logging.model;

import com.codingchili.core.logging.AbstractLogger;
import com.codingchili.core.logging.JsonLogger;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.storage.JsonStorable;
import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 * <p>
 * Logs to an output storage.
 */
public class StorageLogger extends AbstractLogger implements JsonLogger {
    private final LogContext context;

    public StorageLogger(LogContext context, Class aClass) {
        super(context, aClass);
        this.context = context;
    }

    public Logger log(JsonObject data) {
        JsonStorable item = new JsonStorable();
        item.mergeIn(data);

        if (context.storageEnabled()) {
            context.storage().put(item, result -> {
                if (result.failed()) {
                    throw new RuntimeException(result.cause());
                }
            });
        }
        return this;
    }

    @Override
    public void close() {
        // no-op.
    }
}
