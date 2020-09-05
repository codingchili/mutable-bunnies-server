package com.codingchili.instance.model.entity;

/**
 * @author Robin Duda
 *
 * Configuration used to specify where a pre-defined entity will be spawned on a map.
 */
public class SpawnConfig {
    private String id;
    private Point point;

    /**
     * @return Name (id) of the entity to spawn.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id of the entity to spawn.
     */
    public void setId(String id) {
        this.id = id;
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