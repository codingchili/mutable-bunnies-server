package com.codingchili.social.configuration;

import com.codingchili.social.model.*;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.*;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;

/**
 * @author Robin Duda
 * <p>
 * Context wrapper for the social service.
 */
public class SocialContext extends SystemContext {
    private OnlineDB online;
    private AsyncFriendStore friends;
    private TokenFactory factory;

    private SocialContext(CoreContext core) {
        super(core);
        this.factory = new TokenFactory(core, settings().getClientSecret());
        this.online = new OnlineDB(this);
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
                        context.setFriends(new FriendsDB(storage.result(), context.online()));
                        future.complete(context);
                    } else {
                        future.fail(storage.cause());
                    }
                });
        return future;
    }

    private void setFriends(AsyncFriendStore db) {
        this.friends = db;
    }

    /**
     * @return database used to store friend relations.
     */
    public AsyncFriendStore friends() {
        return friends;
    }


    /**
     * @return in-memory database for tracking online accounts.
     */
    public OnlineDB online() {
        return online;
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

    /**
     * Sends a client message to all realms.
     *
     * @param target the receiver account id of the message.
     * @param message the message to send.
     */
    public CompositeFuture send(String target, Object message) {
        List<Future> futures = new ArrayList<>();
        for (String realm : online.realms(target)) {
            Future<Void> future = Future.future();
            futures.add(future);
            bus().request(realm, Serializer.json(message), done -> {
                if (done.succeeded()) {
                    future.complete();
                } else {
                    future.fail(done.cause());
                }
            });
        }
        return CompositeFuture.all(futures);
    }
}
