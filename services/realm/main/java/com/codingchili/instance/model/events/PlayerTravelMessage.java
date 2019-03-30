package com.codingchili.instance.model.events;

import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.transport.ReceivableMessage;

/**
 * Message sent from an instance to a realm to request player transfer.
 */
public class PlayerTravelMessage implements ReceivableMessage {
    private PlayerCreature player;
    private String instance;

    public PlayerTravelMessage() {}

    public PlayerTravelMessage(PlayerCreature player, String instance) {
        this.player = player;
        this.instance = instance;
    }

    public PlayerCreature getPlayer() {
        return player;
    }

    public void setPlayer(PlayerCreature player) {
        this.player = player;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    @Override
    public String target() {
        return player.getAccount();
    }

    @Override
    public String route() {
        return "travel";
    }

}
