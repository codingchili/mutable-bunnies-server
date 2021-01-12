package com.codingchili.instance.model.admin;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * Exception on missing admin authorization.
 */
public class AuthorizationException extends CoreRuntimeException {
    public AuthorizationException() {
        super("Current user not authorized.", ResponseStatus.UNAUTHORIZED);
    }
}
