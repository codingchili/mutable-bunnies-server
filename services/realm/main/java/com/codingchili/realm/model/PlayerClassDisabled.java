package com.codingchili.realm.model;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * Thrown if the given class is not currently available.
 */
public class PlayerClassDisabled extends CoreRuntimeException {
    public PlayerClassDisabled() {
        super("class is not currently available.");
    }
}
