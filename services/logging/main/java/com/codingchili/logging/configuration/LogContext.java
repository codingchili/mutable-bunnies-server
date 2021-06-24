package com.codingchili.logging.configuration;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import com.codingchili.core.context.*;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.*;

/**
 * @author Robin Duda
 * <p>
 * Context used by logging handlers.
 */
public class LogContext extends SystemContext implements ServiceContext {
    private AsyncStorage<JsonStorable> storage;
    private TokenFactory clientFactory;
    private TokenFactory serverFactory;

    public LogContext(CoreContext context, Promise<Void> promise) {
        super(context);

        clientFactory = new TokenFactory(this, service().getClientSecret());
        serverFactory = new TokenFactory(this, service().getLoggingSecret());

        if (storageEnabled()) {
            new StorageLoader<JsonStorable>(context)
                    .withPlugin(service().getPlugin())
                    .withValue(JsonStorable.class)
                    .withDB(service().getDb())
                    .withCollection(service().getCollection())
                    .withProperties(service().getElastic())
                    .build(result -> {
                        if (result.succeeded()) {
                            storage = result.result();
                            promise.complete();
                        } else {
                            promise.fail(result.cause());
                        }
                    });
        } else {
            promise.complete();
        }
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

    public boolean storageEnabled() {
        return service().getStorage();
    }

    public Future<Void> verifyClientToken(Token token) {
        return clientFactory.verify(token);
    }

    public Future<Void> verifyServerToken(Token token) {
        return serverFactory.verify(token);
    }
}
