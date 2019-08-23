package com.codingchili.social.model;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * Thrown when target player is already in a party.
 */
public class TargetAlreadyInPartyException extends CoreRuntimeException {

    public TargetAlreadyInPartyException(String id) {
        super(String.format("%s is already in a party.", id));
    }
}
