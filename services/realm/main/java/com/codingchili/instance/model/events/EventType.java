package com.codingchili.instance.model.events;

/**
 * @author Robin Duda
 */
public enum EventType {
    spell,
    spellstate,
    move,
    death,
    affliction,
    join,   // sent to client for JOIN requests.
    spawn,
    update,
    chat,
    shutdown,
    damage,
    stats,
    save,
    equip_item,
    use_item,
    drop_item,
    unequip_item,
    loot_item,
    loot_list,
    dialog,
    any
}