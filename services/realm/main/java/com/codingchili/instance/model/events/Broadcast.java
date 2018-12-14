package com.codingchili.instance.model.events;

/**
 * @author Robin Duda
 */
public enum Broadcast {
    // publish to entities in the same grid cell: interaction etc.
    ADJACENT,

    // publish to entities on the cells that are in the same network partition: spells, CHAT.
    PARTITION,

    // publish to all entities in the instance: join/leave events.
    GLOBAL,

    // event will not be broadcast at all - use if event is only used for direct messages.
    NONE
}
