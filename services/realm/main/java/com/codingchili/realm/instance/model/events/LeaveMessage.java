package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.controller.RealmRequest;
import com.codingchili.realm.instance.transport.ControlMessage;

import static com.codingchili.common.Strings.CLIENT_INSTANCE_LEAVE;
import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 */
public class LeaveMessage implements ControlMessage {
    private RealmRequest request;
    private String accountName;
    private String playerName;

    public LeaveMessage() {}

    public LeaveMessage(RealmRequest request) {
        this.request = request;
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

    @Override
    public String getTarget() {
        return request.connected();
    }
}
