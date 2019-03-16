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
    protected transient Set<String> interactions = new HashSet<>();
    private String id = UUID.randomUUID().toString();
    protected Model model = new Model() {};
    protected Map<String, Object> attributes = new HashMap<>();
    protected String name = "<no name>";
    protected Vector vector = new Vector();

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
    public Vector getVector() {
        return vector;
    }

    public Entity setVector(Vector vector) {
        this.vector = vector;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public Entity setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Set<String> getInteractions() {
        return interactions;
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
