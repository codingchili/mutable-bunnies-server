package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.transport.ReceivableMessage;

import static com.codingchili.common.Strings.CLIENT_INSTANCE_LEAVE;

/**
 * @author Robin Duda
 *
 * Message emitted when a client leaves the game.
 */
public class LeaveMessage implements ReceivableMessage {
    private String playerName;
    private String accountName;

    public String getAccountName() {
        return accountName;
    }

    public LeaveMessage setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    public String getPlayerName() {
        return playerName;
    }

    public LeaveMessage setPlayerName(String playerName) {
        this.playerName = playerName;
        return this;
    }

    @Override
    public String target() {
        return playerName;
    }

    @Override
    public String route() {
        return CLIENT_INSTANCE_LEAVE;
    }
}
