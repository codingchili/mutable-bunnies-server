package com.codingchili.logging.configuration;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.core.storage.JsonStorable;
import com.codingchili.core.storage.StorageLoader;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 * <p>
 * Context used by logging handlers.
 */
public class LogContext extends SystemContext implements ServiceContext {
    private AsyncStorage<JsonStorable> storage;
    private TokenFactory clientFactory;
    private TokenFactory serverFactory;

    public LogContext(CoreContext context, Future<Void> future) {
        super(context);

        clientFactory = new TokenFactory(this, service().getClientSecret());
        serverFactory = new TokenFactory(this, service().getLoggingSecret());

        new StorageLoader<JsonStorable>(context)
                .withPlugin(service().getPlugin())
                .withValue(JsonStorable.class)
                .withDB(service().getDb())
                .withCollection(service().getCollection())
                .withProperties(service().getElastic())
                .build(result -> {
                    if (result.succeeded()) {
                        storage = result.result();
                        future.complete();
                    } else {
                        future.fail(result.cause());
                    }
                });
    }

    public AsyncStorage<JsonStorable> storage() {
        return storage;
    }

    public LogServerSettings service() {
        return LogServerSettings.get();
    }

    public boolean consoleEnabled() {
        return service().getConsole();
    }

    public Future<Void> verifyClientToken(Token token) {
        return clientFactory.verify(token);
    }

    public Future<Void> verifyServerToken(Token token) {
        return serverFactory.verify(token);
    }
}
