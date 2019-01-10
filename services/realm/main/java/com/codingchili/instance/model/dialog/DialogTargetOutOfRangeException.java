package com.codingchili.instance.model.dialog;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * When the target of the dialog is out of range.
 */
public class DialogTargetOutOfRangeException extends CoreRuntimeException {

    public DialogTargetOutOfRangeException() {
        super("Move closer to interact.");
    }
}
