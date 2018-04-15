package com.codingchili.realm.instance.transport;

import com.codingchili.realm.instance.model.events.EventType;

/**
 * @author Robin Duda
 *
 * Interface for messages passed from an instance to the realm - to be forwarded to a player.
 */
public interface PlayerMessage {

    /**
     * @return the account name of the player to receive the message.
     */
    String receiver();

    /**
     * @return the type of the event.
     */
    EventType type();
}
