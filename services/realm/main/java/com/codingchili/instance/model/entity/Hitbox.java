package com.codingchili.instance.model.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Duda
 */
public class Hitbox {
    private List<Point> points = new ArrayList<>();
    private HitboxType type = HitboxType.rectangular;

    public Hitbox() {}

    public HitboxType getType() {
        return type;
    }

    public void setType(HitboxType type) {
        this.type = type;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }
}
