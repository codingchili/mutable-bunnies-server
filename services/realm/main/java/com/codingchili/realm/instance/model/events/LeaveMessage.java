package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.controller.RealmRequest;

import static com.codingchili.common.Strings.CLIENT_INSTANCE_LEAVE;
import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 */
public class LeaveMessage implements ControlMessage {
    private String accountName;
    private String playerName;

    public LeaveMessage() {}

    public LeaveMessage(RealmRequest request) {
        this.accountName = request.account();
        this.playerName = request.connection().getProperty(ID_NAME).orElse(ID_UNDEFINED);
    }

    public String getAccountName() {
        return accountName;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String getRoute() {
        return CLIENT_INSTANCE_LEAVE;
    }
}
