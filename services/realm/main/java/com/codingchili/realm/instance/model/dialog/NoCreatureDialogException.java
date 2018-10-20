package com.codingchili.realm.instance.model.dialog;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * When the player has attempted to start a dialog with another entity which does not support dialogs.
 *
 * This can happen if the player tries to run a scripted dialog against an arbitrary entity.
 */
public class NoCreatureDialogException extends CoreRuntimeException {

    public NoCreatureDialogException(String targetId) {
        super(String.format("Creature with id '%s' has no dialog.", targetId));
    }

}
