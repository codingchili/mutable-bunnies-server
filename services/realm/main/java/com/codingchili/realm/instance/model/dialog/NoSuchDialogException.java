package com.codingchili.realm.instance.model.dialog;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * When loading a dialog specified by an npc that does not exist in the dialog database.
 *
 * Typically a configuration error, because clients should never be able to specify this.
 */
public class NoSuchDialogException extends CoreRuntimeException {

    public NoSuchDialogException(String id) {
        super(String.format("Dialog with id '%s' not found.", id));
    }

}
