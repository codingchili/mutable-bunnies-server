package com.codingchili.realm.instance.model.dialog;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * When the target of the dialog is out of range.
 */
public class DialogTargetOutOfRangeException extends CoreRuntimeException {


    public DialogTargetOutOfRangeException(String targetId) {
        super(String.format("Target '%s' is out of range, unable to initiate dialog", targetId));
    }
}
