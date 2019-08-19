package com.codingchili.instance.model.questing;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * A quest is not found by it's ID or is invalid because of consistency issues etc.
 */
public class InvalidQuestException extends CoreRuntimeException {
    public InvalidQuestException(String description) {
        super(description);
    }
}
