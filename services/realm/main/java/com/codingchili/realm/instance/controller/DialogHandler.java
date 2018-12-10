package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.dialog.DialogRequest;
import com.codingchili.realm.instance.model.events.ChatEvent;
import com.codingchili.realm.instance.model.npc.DialogEngine;
import com.codingchili.realm.instance.model.spells.SpellResult;
import com.codingchili.realm.instance.model.spells.SpellTarget;
import com.codingchili.realm.instance.transport.InstanceRequest;
import io.vertx.core.json.JsonObject;

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

        if (message.startsWith("/afflict")) {
            String[] args = message.replaceFirst("/afflict", "").trim().split(" ");
            game.spells().afflict(game.getById(args[1]), game.getById(args[2]), args[0]);
        }

        if (message.startsWith("/spell")) {
            JsonObject response = new JsonObject();
            SpellResult result = game.spells().cast(game.getById(request.target()),
                    new SpellTarget().setTargetId(request.target()), message.split(" ")[1]);
            response.put("spellResult", result);
            request.write(response);
        }

        game.publish(new ChatEvent(game.getById(request.target()), message));
    }

    @Api
    public void say(InstanceRequest request) {
        request.result(dialogs.say(request.raw(DialogRequest.class))
                .map(DialogRequest::from));
    }

    @Api
    public void talk(InstanceRequest request) {
        request.result(dialogs.start(request.raw(DialogRequest.class))
                .map(DialogRequest::from));
    }

    @Override
    public void onPlayerLeave(String id) {
        dialogs.close(id);
    }
}
