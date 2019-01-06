package com.codingchili.instance.model.dialog;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 */
public class DialogTargetBusyException extends CoreRuntimeException {

    public DialogTargetBusyException() {
        super("Target is busy.");
    }
}