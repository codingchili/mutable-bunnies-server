package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Vector;

/**
 * @author Robin Duda
 *
 * A movement event emitted by the movement handler.
 */
public class MovementEvent implements Event {
    private Vector vector;
    private String creatureId;

    public MovementEvent() {}

    /**
     * @param vector the new vector.
     * @param creatureId the creature that has updated its vector.
     */
    public MovementEvent(Vector vector, String creatureId) {
        this.creatureId = creatureId;
        this.vector = vector;
    }

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public String getCreatureId() {
        return creatureId;
    }

    public void setCreatureId(String creatureId) {
        this.creatureId = creatureId;
    }

    @Override
    public EventType getRoute() {
        return EventType.move;
    }
}
