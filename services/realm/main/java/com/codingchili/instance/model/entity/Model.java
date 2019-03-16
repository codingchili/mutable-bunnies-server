package com.codingchili.instance.model.entity;

import java.util.List;

/**
 * @author Robin Duda
 * <p>
 * A graphical representation of an object to be used by the client and hit detection.
 */
public class Model {
    private String name;
    private float scale;
    private boolean blocking;
    private List<Point> hitbox;

    /**
     * @return the graphical representation of the model, a sprite for example.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the scale at which the hitbox and sprite should be rendered.
     */
    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * @return indicates if the model is penetrable by a player or not.
     */
    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    /**
     * @return a list of points that create a bounding box.
     */
    public List<Point> getHitbox() {
        return hitbox;
    }

    public void setHitbox(List<Point> hitbox) {
        this.hitbox = hitbox;
    }
}
