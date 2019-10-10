package com.codingchili.instance.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 *
 * A point or a dimension.
 */
public class Point {
    private int x;
    private int y;

    public Point() {
    }

    /**
     * @param x the x axis (width).
     * @param y the y axis (height).
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return position or width for the x axis.
     */
    public int getX() {
        return x;
    }

    /**
     * @param x set the axle length or x position of this point.
     * @return fluent
     */
    public Point setX(int x) {
        this.x = x;
        return this;
    }

    /**
     * @return position or heigeht for the y axis.
     */
    public int getY() {
        return y;
    }

    /**
     * @param y set the axle length or y position of this point.
     * @return fluent
     */
    public Point setY(int y) {
        this.y = y;
        return this;
    }

    /**
     * @return the length of the longest axis.
     */
    @JsonIgnore
    public int getHighestAxis() {
        return Math.max(x, y);
    }
}
