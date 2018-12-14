package com.codingchili.instance.model.entity;

import com.codingchili.instance.model.events.Event;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.RoleMap;

/**
 * @author Robin Duda
 *
 * A protocol to map events to entities.
 */
public class EventProtocol extends Protocol<Event> {

    public EventProtocol() {
        setRole(RoleMap.get(RoleMap.PUBLIC));
    }

    /**
     * @param handler a receiver to handle events for this events protocol.
     */
    public EventProtocol(Receiver<Event> handler) {
        setRole(RoleMap.get(RoleMap.PUBLIC));
        annotated(handler);
    }
}
