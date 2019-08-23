package com.codingchili.common;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonProperty("target")
    String target();

    /**
     * @return the name of the event.
     */
    @JsonProperty("route")
    String route();
}
