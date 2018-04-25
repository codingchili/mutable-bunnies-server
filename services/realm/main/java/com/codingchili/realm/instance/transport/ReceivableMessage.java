package com.codingchili.realm.instance.transport;

/**
 * @author Robin Duda
 * <p>
 * Must be implemented by all message objects passed to clients.
 */
public interface ReceivableMessage {

    /**
     * @return the connection identifier on the realm server - typically the
     * clients account name.
     */
    String target();

    /**
     * @return the name of the event.
     */
    String route();
}
