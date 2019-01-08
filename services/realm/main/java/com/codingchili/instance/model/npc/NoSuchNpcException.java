package com.codingchili.instance.model.npc;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * Thrown when an npc is not found.
 */
public class NoSuchNpcException extends CoreRuntimeException {

    public NoSuchNpcException(String id) {
        super("Npc with the given id '" + id + "' not in database.");
    }
}
