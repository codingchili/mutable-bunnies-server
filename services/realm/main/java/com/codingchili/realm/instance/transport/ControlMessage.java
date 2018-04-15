package com.codingchili.realm.instance.transport;

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

    /**
     * @return the instance the control message is ment for.
     */
    String getTarget();
}
