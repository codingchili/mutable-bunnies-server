package com.codingchili.social.model;

import com.codingchili.common.ReceivableMessage;
import com.codingchili.social.controller.SocialRequest;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * <p>
 * Friend online event emitted to all realms that friends are logged onto.
 */
public class FriendOnlineEvent implements ReceivableMessage {
    private boolean online;
    private String target;
    private String realm;
    private String friend;

    public FriendOnlineEvent(SocialRequest request, boolean online) {
        this.online = online;
        this.realm = request.realm();
        this.friend = request.target();
    }

    public FriendOnlineEvent setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getRealm() {
        return this.realm;
    }

    public String getFriend() {
        return this.friend;
    }

    @Override
    public String target() {
        return target;
    }

    @Override
    public String route() {
        return (online) ? SOCIAL_ONLINE : SOCIAL_OFFLINE;
    }
}
