package com.codingchili.instance.model.npc;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * Thrown when an npc is not found.
 */
public class NoSuchStructureException extends CoreRuntimeException {

    public NoSuchStructureException(String id) {
        super("Structure with the given id '" + id + "' not in database.");
    }
}
