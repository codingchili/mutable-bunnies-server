package com.codingchili.realm.instance.model.dialog;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * Use when a player has attempted to continue a dialog that is not in an active state.
 *
 * This happens if the client attempts to cheat or is out of sync.
 */
public class NoActiveDialogException extends CoreRuntimeException {
    public static final Throwable INSTANCE = new NoActiveDialogException();

    public NoActiveDialogException() {
        super("No active dialog.");
    }
}
