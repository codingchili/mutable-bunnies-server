package com.codingchili.social.controller;

import com.codingchili.social.configuration.SocialContext;
import com.codingchili.social.model.AsyncFriendStore;

import com.codingchili.core.protocol.Api;
import com.codingchili.core.protocol.Roles;

import static com.codingchili.core.protocol.RoleMap.USER;

/**
 * @author Robin Duda
 * <p>
 * Handles requests for friendslists.
 */
@Roles(USER)
public class FriendHandler implements SocialServiceHandler {
    private AsyncFriendStore friends;

    public FriendHandler(SocialContext context) {
        friends = context.db();
    }

    @Api
    public void friend_request(SocialRequest request) {
        friends.request(request.account(), request.friend())
                .setHandler(request::result);
    }

    @Api
    public void friend_accept(SocialRequest request) {
        friends.accept(request.account(), request.friend())
                .setHandler(request::result);
    }

    @Api
    public void friend_reject(SocialRequest request) {
        friends.reject(request.account(), request.friend())
                .setHandler(request::result);
    }

    @Api
    public void friend_list(SocialRequest request) {
        friends.list(request.account())
                .setHandler(request::result);
    }

    @Api
    public void friend_pending(SocialRequest request) {
        friends.pending(request.account())
                .setHandler(request::result);
    }

    @Api
    public void friend_remove(SocialRequest request) {
        friends.remove(request.account(), request.friend())
                .setHandler(request::result);
    }

    @Api
    public void friend_suggest(SocialRequest request) {
        friends.suggestions(request.friend())
                .setHandler(request::result);
    }
}
