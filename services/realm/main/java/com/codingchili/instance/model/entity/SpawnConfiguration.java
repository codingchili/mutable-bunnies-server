package com.codingchili.instance.model.entity;

/**
 * @author Robin Duda
 *
 * Configuration used to specify where a pre-defined entity will be spawned on a map.
 */
public class SpawnConfiguration {
    private String entity;
    private Point point;

    /**
     * @return Name (id) of the entity to spawn.
     */
    public String getEntity() {
        return entity;
    }

    /**
     * @param entity the id of the entity to spawn.
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }

    /**
     * @return the point where the entity will be spawned.
     */
    public Point getPoint() {
        return point;
    }

    /**
     * @param point the point where the entity will be spawned.
     */
    public void setPoint(Point point) {
        this.point = point;
    }
}
