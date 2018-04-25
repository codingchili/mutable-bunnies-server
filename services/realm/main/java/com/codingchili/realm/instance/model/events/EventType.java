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
    any
}