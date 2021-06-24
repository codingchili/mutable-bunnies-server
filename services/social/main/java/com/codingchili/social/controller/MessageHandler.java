package com.codingchili.social.controller;

import com.codingchili.social.configuration.SocialContext;
import com.codingchili.social.model.*;
import io.vertx.core.*;

import java.util.*;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.Api;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 * <p>
 * Handles messaging between friends.
 */
public class MessageHandler implements SocialServiceHandler {
    private AsyncFriendStore friends;
    private OnlineDB online;
    private SocialContext context;

    /**
     * @param context the context to run the handler on.
     */
    public MessageHandler(SocialContext context) {
        this.context = context;
        this.online = context.online();
        this.friends = context.friends();
    }

    @Api
    public void friend_message(SocialRequest request) {
        String friend = request.friend();

        friends.list(request.account()).onComplete(done -> {
            if (done.succeeded()) {
                FriendList list = done.result();

                if (list.isFriend(friend)) {
                    if (online.is(friend)) {

                        sendRealm(online.realms(friend), new FriendMessage(request)).onComplete(msg -> {
                            if (msg.succeeded()) {
                                request.accept();
                            } else {
                                request.error(msg.cause());
                            }
                        });

                    } else {
                        request.error(new CoreRuntimeException("Friend is not online."));
                    }
                } else {
                    request.error(new CoreRuntimeException("Not a friend."));
                }
            } else {
                request.error(done.cause());
            }
        });
    }

    private CompositeFuture sendRealm(Set<String> realms, FriendMessage message) {
        List<Future> futures = new ArrayList<>();
        for (String realm : realms) {
            Promise<Void> promise = Promise.promise();
            futures.add(promise.future());
            context.bus().request(realm, Serializer.json(message), done -> {
                if (done.succeeded()) {
                    promise.complete();
                } else {
                    promise.fail(done.cause());
                }
            });
        }
        return CompositeFuture.all(futures);
    }
}
