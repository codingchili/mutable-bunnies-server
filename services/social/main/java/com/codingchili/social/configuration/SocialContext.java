package com.codingchili.social.configuration;

import com.codingchili.social.model.*;
import io.vertx.core.Future;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;

/**
 * @author Robin Duda
 * <p>
 * Context wrapper for the social service.
 */
public class SocialContext extends SystemContext {
    private AsyncFriendStore db;
    private TokenFactory factory;

    private SocialContext(CoreContext core) {
        super(core);
        this.factory = new TokenFactory(core, settings().getClientSecret());
    }

    /**
     * Creates the social context and asynchronously sets up databases.
     *
     * @param core the core context to create the social context on.
     * @return future.
     */
    public static Future<SocialContext> create(CoreContext core) {
        Future<SocialContext> future = Future.future();
        SocialContext context = new SocialContext(core);

        new StorageLoader<FriendList>(core)
                .withPlugin(context.settings().getStorage())
                .withValue(FriendList.class)
                .build(storage -> {
                    if (storage.succeeded()) {
                        context.setDb(new FriendsDB(storage.result()));
                        future.complete(context);
                    } else {
                        future.fail(storage.cause());
                    }
                });
        return future;
    }

    private void setDb(AsyncFriendStore db) {
        this.db = db;
    }

    /**
     * @return database used to store friend relations.
     */
    public AsyncFriendStore db() {
        return db;
    }

    /**
     * @param token a client token to verify the signature of.
     * @return future.
     */
    public Future<Void> verify(Token token) {
        return factory.verify(token);
    }

    /**
     * @return the settings for this social service.
     */
    public SocialSettings settings() {
        return Configurations.get(SocialSettings.PATH, SocialSettings.class);
    }
}
