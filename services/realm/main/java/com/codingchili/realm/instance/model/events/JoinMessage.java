package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.controller.RealmRequest;
import com.codingchili.realm.instance.model.entity.PlayerCreature;
import com.codingchili.realm.instance.transport.ControlMessage;

import static com.codingchili.common.Strings.CLIENT_INSTANCE_JOIN;

/**
 * @author Robin Duda
 */
public class JoinMessage implements ControlMessage {
    private RealmRequest request;
    private PlayerCreature player;
    private String realmName;

    public JoinMessage() {}

    public JoinMessage(RealmRequest request) {
        this.request = request;
    }

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

    public String getRoute() {
        return CLIENT_INSTANCE_JOIN;
    }

    @Override
    public String getTarget() {
        return request.connected();
    }
}
