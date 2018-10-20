package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.dialog.*;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.entity.Vector;
import com.codingchili.realm.instance.model.events.ChatEvent;
import com.codingchili.realm.instance.model.npc.Npc;
import com.codingchili.realm.instance.model.spells.SpellResult;
import com.codingchili.realm.instance.model.spells.SpellTarget;
import com.codingchili.realm.instance.transport.InstanceRequest;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

import com.codingchili.core.protocol.Api;

import static com.codingchili.core.configuration.CoreStrings.ID_MESSAGE;


/**
 * @author Robin Duda
 * <p>
 * Handles player-to-npc dialogs and chat messages.
 */
public class DialogHandler implements GameHandler {
    public static final int DIALOG_RANGE = 128;
    private Map<String, ActiveDialog> dialogs = new HashMap<>();
    private DialogDB dialogDB;
    private GameContext game;

    public DialogHandler(GameContext game) {
        this.game = game;
        this.dialogDB = new DialogDB(game.getInstance());
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
        DialogRequest query = request.raw(DialogRequest.class);
        ActiveDialog active = dialogs.get(request.target());

        if (active != null) {
            active.say(query.getNext());

            if (active.isEnded()) {
                dialogs.remove(request.target());
            }
            request.write(DialogRequest.from(active));
        } else {
            request.error(NoActiveDialogException.INSTANCE);
        }
    }

    @Api
    public void talk(InstanceRequest request) {
        DialogRequest query = request.raw(DialogRequest.class);

        Creature source = game.getById(request.target());
        Creature target = game.getById(query.getTargetId());

        Vector vector = source.getVector().copy()
                .setSize(DIALOG_RANGE);

        if (game.creatures().radius(vector).contains(target)) {
            if (target instanceof Npc) {
                Dialog dialog = dialogDB.getById(((Npc) target).getDialogId());
                ActiveDialog active = new ActiveDialog(game, dialog, source, target);
                dialogs.put(request.target(), active);
                request.write(DialogRequest.from(active));
            } else {
                request.error(new NoCreatureDialogException(query.getTargetId()));
            }
        } else {
            request.error(new DialogTargetOutOfRangeException(query.getTargetId()));
        }
    }

    @Override
    public void onPlayerLeave(String id) {
        dialogs.remove(id);
    }
}
