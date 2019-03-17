package com.codingchili.instance.model.entity;

/**
 * @author Robin Duda
 *
 * Configuration used to specify where a pre-defined entity will be spawned on a map.
 */
public class SpawnConfiguration {
    private String name;
    private Point point;

    /**
     * @return Name (id) of the entity to spawn.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the id of the entity to spawn.
     */
    public void setName(String name) {
        this.name = name;
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
