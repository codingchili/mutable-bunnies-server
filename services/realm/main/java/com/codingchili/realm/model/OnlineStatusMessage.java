package com.codingchili.realm.model;

import com.codingchili.common.ReceivableMessage;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * <p>
 * Messages sent to the social service when an account connects to a realm.
 */
public class OnlineStatusMessage implements ReceivableMessage {
    private String account;
    private String realm;
    private boolean online;

    /**
     * @param account the account that is connecting or disconnecting.
     * @param realm   the realm that the account is connecting to.
     * @param online  true if the event is a connect event, otherwise false for disconnects.
     */
    public OnlineStatusMessage(String account, String realm, boolean online) {
        this.account = account;
        this.realm = realm;
        this.online = online;
    }

    public String getRealm() {
        return realm;
    }

    public boolean isOnline() {
        return online;
    }

    @Override
    public String target() {
        return account;
    }

    @Override
    public String route() {
        return (online) ? SOCIAL_ONLINE : SOCIAL_OFFLINE;
    }
}
