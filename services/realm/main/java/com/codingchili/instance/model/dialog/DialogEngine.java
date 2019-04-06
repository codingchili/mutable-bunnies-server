package com.codingchili.instance.model.dialog;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.events.ChatEvent;
import io.vertx.core.Future;

import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Handles dialog-based player interaction. Dialogs can be between
 * a player and an NPC or between a player and a game object.
 */
public class DialogEngine {
    private static final int DIALOG_RANGE = 164;
    private static final String DIALOG_ID = "DIALOG_ID";
    private static final String MISSING = "undefined";
    private Map<String, ActiveDialog> dialogs = new HashMap<>();
    private DialogDB dialogDB;
    private GameContext game;

    /**
     * @param game the context the dialog engine is valid for.
     */
    public DialogEngine(GameContext game) {
        dialogDB = new DialogDB(game.instance());
        this.game = game;
    }

    /**
     * Player action to advance the dialog.
     *
     * @param request  a request to alter state of the dialog.
     * @param sourceId the dialog initiator that advances the dialog.
     * @return the current state of the dialog.
     */
    public Future<ActiveDialog> say(DialogRequest request, String sourceId) {
        ActiveDialog active = dialogs.get(sourceId);

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
     * Triggers a dialog on the specified target id. This skips the range check and the dialog
     * busy check. It can be triggered when entering a new area, encountering a new npc etc.
     *
     * @param sourceId the npc or entity that is initiating the dialog. for monologues the target and
     *                 source may refer to the same entity.
     * @param targetId the player that the dialog will be trigged on.
     * @param dialogId the id of the dialog to be triggered.
     * @return future.
     */
    public Future<ActiveDialog> trigger(String sourceId, String targetId, String dialogId) {
        Future<ActiveDialog> future = Future.future();
        Optional<Dialog> dialog = dialogDB.getById(dialogId);
        Entity source = game.getById(sourceId);
        Entity target = game.getById(targetId);

        if (dialog.isPresent()) {
            ActiveDialog active = new ActiveDialog(game, dialog.get(), target, source);
            dialogs.put(targetId, active);
            target.handle(DialogRequest.from(active));
            future.complete(active);
        } else {
            game.getLogger(getClass()).event("dialog.engine")
                    .put("id", dialogId)
                    .send("failed to trigger dialog, id reference failure.");
        }
        return future;
    }

    /**
     * Starts a dialog between a player and a target entity.
     *
     * @param request  dialog request
     * @param sourceId the creature initiating the dialog.
     * @return the current state of the dialog. failed if the
     * target does not support dialogs, is out of range or
     * if the dialog used by the target was not found.
     */
    public Future<ActiveDialog> start(DialogRequest request, String sourceId) {
        Creature source = game.getById(sourceId);
        Creature target = game.getById(request.getTargetId());

        String dialogId = (String) target.getAttributes().getOrDefault(DIALOG_ID, MISSING);

        // allow interaction with both other creatures and entities.
        if (targetInRange(source, target)) {
            Optional<Dialog> dialog = dialogDB.getById(dialogId);

            if (dialog.isPresent()) {
                Dialog loaded = dialog.get();

                if (loaded.enabled(source, target)) {
                    ActiveDialog active = new ActiveDialog(game, dialog.get(), source, target);
                    dialogs.put(sourceId, active);
                    return Future.succeededFuture(active);
                } else {
                    return Future.failedFuture(new DialogTargetBusyException());
                }
            } else {
                return Future.failedFuture(new NoSuchDialogException(dialogId));
            }
        } else {
            return Future.failedFuture(new InteractionOutOfRangeException());
        }
    }

    /**
     * Closes any active dialogs for the given entity id.
     *
     * @param id of the initiating entity.
     */
    public void leave(String id) {
        dialogs.remove(id);
    }

    /**
     * Registers a dialog handler on the given entity.
     *
     * @param target   the entity to register the dialog handler on.
     * @param dialogId the id of the dialog to use.
     */
    public void register(Entity target, String dialogId) {
        target.getInteractions().add(Interaction.DIALOG);
        target.getAttributes().put(DIALOG_ID, dialogId);
    }

    private boolean targetInRange(Entity source, Entity target) {
        Vector vector = target.getVector().copy()
                .setSize(DIALOG_RANGE);

        return game.creatures().radius(vector).contains(source);
    }

    /**
     * Emits a chat message from the given creature.
     *
     * @param target  the talking entity.
     * @param message the text of the message to send.
     */
    public void say(String target, String message) {
        game.publish(new ChatEvent(game.getById(target), message));
    }
}
