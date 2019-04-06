package com.codingchili.instance.model.items;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * Thrown when looting has been requested for an empty loot container.
 */
public class LootableEmptyException extends CoreRuntimeException {

    public LootableEmptyException() {
        super("There is nothing to loot.");
    }
}
