package com.codingchili.social.model;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * When an invite has expired when its accepted or declined.
 */
public class MissingPartyInviteException extends CoreRuntimeException {

    public MissingPartyInviteException(String id) {
        super(String.format("Party invite expired. [%s]", id));
    }

}
