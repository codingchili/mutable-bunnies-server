package com.codingchili.instance.model.events;

import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.transport.ReceivableMessage;

import static com.codingchili.common.Strings.CLIENT_INSTANCE_JOIN;

/**
 * @author Robin Duda
 */
public class JoinMessage implements ReceivableMessage {
    private PlayerCreature player;
    private String realmName;

    public JoinMessage setPlayer(PlayerCreature player) {
        this.player = player;
        return this;
    }

    public PlayerCreature getPlayer() {
        return player;
    }

    public JoinMessage setRealmName(String realmName) {
        this.realmName = realmName;
        return this;
    }

    public String getRealmName() {
        return realmName;
    }

    @Override
    public String target() {
        return player.getName();
    }

    @Override
    public String route() {
        return CLIENT_INSTANCE_JOIN;
    }
}
