package com.codingchili.realm.instance.model.events;

/**
 * @author Robin Duda
 *
 * Interface implemented by messages sent between the instance and the realm.
 */
public interface ControlMessage {

    /**
     * @return a string matching the handler to be executed when the message is received.
     */
    String getRoute();
}
