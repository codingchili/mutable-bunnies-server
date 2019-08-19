package com.codingchili.instance.model.events;

/**
 * @author Robin Duda
 */
public enum EventType {
    spell,
    spellstate,
    cleanse,

    affliction,

    move,

    death,
    join,
    spawn,
    update,

    chat,
    shutdown,
    damage,
    stats,
    save,

    dialog,

    equip_item,
    use_item,
    drop_item,
    unequip_item,
    loot_item,
    loot_list,
    loot_unsubscribe,
    inventory_update,

    quest_accepted,
    quest_complete,
    quest_update,

    error,
    any
}