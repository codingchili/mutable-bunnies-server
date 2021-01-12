package com.codingchili.instance.model.events;

/**
 * @author Robin Duda
 */
public enum EventType {
    admin,

    spell,
    spellstate,
    cleanse,

    affliction,

    move,
    animation,

    death,
    join,
    spawn,
    despawn,
    update,

    chat,
    shutdown,
    attribute,
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

    skill_state,
    skill_change,
    skill_info,

    party_request,
    party_leave,
    party_accept,
    party_decline,

    npc_registry,
    structure_registry,

    error,
    any,

    notification
}