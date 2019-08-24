package com.codingchili.instance.controller;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.dialog.DialogRequest;
import com.codingchili.instance.model.dialog.DialogEngine;
import com.codingchili.instance.model.npc.TalkingPerson;
import com.codingchili.instance.model.spells.SpellResult;
import com.codingchili.instance.model.spells.SpellTarget;
import com.codingchili.instance.transport.InstanceRequest;
import io.vertx.core.json.JsonObject;

import java.util.Random;

import com.codingchili.core.protocol.Api;

import static com.codingchili.core.configuration.CoreStrings.ID_MESSAGE;


/**
 * @author Robin Duda
 * <p>
 * Handles player-to-npc dialogs and chat messages.
 */
public class DialogHandler implements GameHandler {
    private DialogEngine dialogs;
    private GameContext game;

    public DialogHandler(GameContext game) {
        this.game = game;
        this.dialogs = game.dialogs();
    }

    @Api
    public void chat(InstanceRequest request) {
        String message = request.data().getString(ID_MESSAGE);
        dialogs.say(request.target(), message);
    }

    @Api
    public void say(InstanceRequest request) {
        request.result(dialogs.say(request.raw(DialogRequest.class), request.target())
                .map(DialogRequest::from));
    }

    @Api
    public void talk(InstanceRequest request) {
        request.result(dialogs.start(request.raw(DialogRequest.class), request.target())
                .map(DialogRequest::from));
    }

    @Api
    public void end(InstanceRequest request) {
        dialogs.leave(request.target());
    }

    @Override
    public void onPlayerLeave(String id) {
        dialogs.leave(id);
    }
}
