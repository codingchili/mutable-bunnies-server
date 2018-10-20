package com.codingchili.realm.instance.model.events;

/**
 * @author Robin Duda
 */
public enum EventType {
    spell,
    move,
    death,
    affliction,
    join,   // sent to client for JOIN requests.
    spawn,
    update,
    chat,
    shutdown,
    damage,
    save,
    equip_item,
    use_item,
    drop_item, unequip_item, loot_item, loot_list, dialog, any
}