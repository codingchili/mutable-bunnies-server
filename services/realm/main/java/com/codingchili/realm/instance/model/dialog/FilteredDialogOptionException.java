package com.codingchili.realm.instance.model.dialog;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * When the chosen dialog option was not available.
 * This happens if the client tries to trick the server or the client is out of sync.
 *
 * We need to filter dialog options when showing them and then again when one
 * is chosen.
 */
public class FilteredDialogOptionException extends CoreRuntimeException {

    public FilteredDialogOptionException(String dialogOption) {
        super(String.format("Dialog option '%s' is not available.", dialogOption));
    }

}
