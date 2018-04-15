package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.entity.Entity;
import com.codingchili.realm.instance.model.events.ChatEvent;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Api;

import static com.codingchili.core.configuration.CoreStrings.ID_MESSAGE;


/**
 * @author Robin Duda
 */
public class DialogHandler implements Receiver<Request> {
    private GameContext game;

    public DialogHandler(GameContext game) {
        this.game = game;
    }

    @Api(route = "chat")
    public void chat(InstanceRequest request) {
        String message = request.data().getString(ID_MESSAGE);

        if (message.startsWith("/afflict")) {
            String[] args = message.replaceFirst("/afflict", "").trim().split(" ");
            game.spells().afflict(game.getById(args[1]), game.getById(args[2]), args[0]);
        }

        game.publish(new ChatEvent(game.getById(request.character()), message));
        request.accept();
    }

    @Override
    public void handle(Request request) {
        // what is this for?
    }
}
