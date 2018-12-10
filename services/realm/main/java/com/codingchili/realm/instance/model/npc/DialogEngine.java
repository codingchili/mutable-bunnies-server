package com.codingchili.realm.instance.model.npc;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.dialog.*;
import com.codingchili.realm.instance.model.entity.*;
import com.codingchili.realm.instance.model.entity.Vector;
import io.vertx.core.Future;

import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Handles dialog-based player interaction. Dialogs can be between
 * a player and an NPC or between a player and a game object.
 */
public class DialogEngine {
    private static final int DIALOG_RANGE = 128;
    public static final String HANDLER_DIALOG = "dialog";
    private Map<String, ActiveDialog> dialogs = new HashMap<>();
    private DialogDB dialogDB;
    private GameContext game;

    /**
     * @param game the context the dialog engine is valid for.
     */
    public DialogEngine(GameContext game) {
        dialogDB = new DialogDB(game.getInstance());
        this.game = game;
    }

    /**
     * Player action to advance the dialog.
     *
     * @param request a request to alter state of the dialog.
     * @return the current state of the dialog.
     */
    public Future<ActiveDialog> say(DialogRequest request) {
        ActiveDialog active = dialogs.get(request.getSource());

        if (active != null) {
            active.say(request.getNext());

            if (active.isEnded()) {
                dialogs.remove(request.getSource());
            }
            return Future.succeededFuture(active);
        } else {
            return Future.failedFuture(NoActiveDialogException.INSTANCE);
        }
    }

    /**
     * Starts a dialog between a player and a target entity.
     *
     * @param request dialog request
     * @return the current state of the dialog. failed if the
     * target does not support dialogs, is out of range or
     * if the dialog used by the target was not found.
     */
    public Future<ActiveDialog> start(DialogRequest request) {
        Creature source = game.getById(request.getSourceId());
        Creature target = game.getById(request.getTargetId());

        // todo protocol.available is not the same as interactions?
        // todo: interactions can has properties or read from property map?
        // don't use the handler approach because it doesn't handle result well?

        // or interactions with configuration?
        /*target.getInteractions().contains("dialog") {
            target.getAttributes().get("DIALOG")
        }*/

        target.handle(request);

        Vector vector = source.getVector().copy()
                .setSize(DIALOG_RANGE);

        if (game.creatures().radius(vector).contains(target)) {
            // how to? register a default handler on the NPC with name "dialog", to
            // be returned as an interaction to the player? this is good because
            // the handler only lives in configuration not on the entity object.
            Optional<String> dialogId = DialogBehaviour.getDialog(target);

            // todo: move this part into the create handler
            // todo: in here: send an event to the handler.
            // this is the EXT way. messages and handlers.

            if (dialogId.isPresent()) {
                Dialog dialog = dialogDB.getById(dialogId.get());
                ActiveDialog active = new ActiveDialog(game, dialog, source, target);
                dialogs.put(request.getSourceId(), active);
                return Future.succeededFuture(active);
            } else {
                return Future.failedFuture(new NoCreatureDialogException(request.getTargetId()));
            }
        } else {
            return Future.failedFuture(new DialogTargetOutOfRangeException(request.getTargetId()));
        }
    }

    /**
     * Registers a dialog handler on the given entity.
     *
     * @param entity   the entity to register the dialog handler on.
     * @param dialogId the id of the dialog to use.
     */
    public void register(Entity entity, String dialogId) {
        //entity.protocol().use(HANDLER_DIALOG, (event) -> start(event.getSource(), dialogId));
    }

    /**
     * Closes any active dialogs for the given entity id.
     *
     * @param id of the initiating entity.
     */
    public void close(String id) {
        dialogs.remove(id);
    }
}
