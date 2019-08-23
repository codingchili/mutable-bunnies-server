package com.codingchili.social.controller;

import com.codingchili.common.Strings;
import com.codingchili.social.configuration.SocialContext;
import com.codingchili.social.model.*;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.*;

import static com.codingchili.core.protocol.RoleMap.PUBLIC;

/**
 * @author Robin Duda
 * <p>
 * Online handler called by realms on connect/disconnect.
 * <p>
 * Note: if a player logs in to a single realm multiple times, then disconnects once
 * the online status will be offline. Should realms prevent this? probably.
 */
@Roles(PUBLIC)
@Address(Strings.ONLINE_SOCIAL_NODE)
public class OnlineHandler implements CoreHandler {
    private Protocol<Request> protocol = new Protocol<>(this);
    private OnlineDB online;
    private AsyncFriendStore friends;
    private SocialContext context;

    /**
     * @param context the context to run the handler on.
     */
    public OnlineHandler(SocialContext context) {
        this.context = context;
        this.online = context.online();
        this.friends = context.friends();
    }

    @Api
    public void social_online(SocialRequest request) {
        online.add(request.target(), request.realm());
        notifyAllFriends(new FriendOnlineEvent(request, true));
    }

    @Api
    public void social_offline(SocialRequest request) {
        if (online.is(request.target())) {
            notifyAllFriends(new FriendOnlineEvent(request, false));

            // leave current parties
            context.party().leave(request.target());
        }
        online.remove(request.target(), request.realm());
    }

    private void notifyAllFriends(FriendOnlineEvent event) {
        friends.list(event.getFriend()).setHandler(list -> {
            list.result().getFriends().forEach(friend -> {
                if (online.is(friend)) {
                    context.send(friend, event.setTarget(friend));
                }
            });
        });
    }

    @Override
    public void handle(Request request) {
        protocol.process(new SocialRequest(request));
    }
}
