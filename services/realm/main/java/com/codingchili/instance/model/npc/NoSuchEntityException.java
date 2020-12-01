package com.codingchili.instance.model.npc;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * Thrown when an npc is not found.
 */
public class NoSuchEntityException extends CoreRuntimeException {

    public NoSuchEntityException(String id) {
        super("Entity with the given id '" + id + "' not in npc/structure database.");
    }
}
