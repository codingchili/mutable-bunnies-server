package com.codingchili.instance.model.dialog;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * When the target of the dialog is out of range.
 */
public class InteractionOutOfRangeException extends CoreRuntimeException {

    public InteractionOutOfRangeException() {
        super("Move closer to interact.");
    }
}
