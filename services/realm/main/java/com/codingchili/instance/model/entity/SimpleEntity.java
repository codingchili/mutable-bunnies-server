package com.codingchili.instance.model.entity;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.events.Event;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

/**
 * @author Robin Duda
 */
public abstract class SimpleEntity implements Entity {
    protected transient GameContext game;
    protected transient EventProtocol protocol = new EventProtocol(this);
    private String id = UUID.randomUUID().toString();
    protected Map<String, Object> attributes = new HashMap<>();
    protected String name = "<no name>";
    protected com.codingchili.instance.model.entity.Vector vector = new com.codingchili.instance.model.entity.Vector();

    @Override
    public void setContext(GameContext game) {
        this.game = game;
    }

    @JsonIgnore
    @Override
    public EventProtocol protocol() {
        return protocol;
    }

    @Override
    public void handle(Event event) {
        protocol.get(event.getRoute().toString()).submit(event);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public com.codingchili.instance.model.entity.Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    @Override
    public Set<String> getInteractions() {
        return protocol.available();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return compareTo(other) == 0;
    }
}
