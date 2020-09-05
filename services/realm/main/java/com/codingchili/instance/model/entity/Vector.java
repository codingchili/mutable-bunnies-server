package com.codingchili.instance.model.entity;

import com.codingchili.instance.context.Ticker;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Robin Duda
 * <p>
 * A vector within the game world.
 * <p>
 * sample use cases
 * - used to represent player positions and movements.
 * - used to cast spells.
 */
public class Vector {
    public static final float ACCELERATION_BASE = 0.4f;
    private static final float ACCELERATION_STEP = (1 - ACCELERATION_BASE);
    private static final float ACCELERATION_TIME = 0.84f;
    private transient Collection<Integer> buckets = new HashSet<>();
    private transient float acceleration = ACCELERATION_BASE;
    private transient boolean dirty = true;
    private transient int index = -1;
    private float velocity = 0f;
    private float direction = 0.0f;
    private int size = 24;
    private float x = -1;
    private float y = -1;

    /**
     * @return the vectors position on the x axis.
     */
    public float getX() {
        return x;
    }

    /**
     * @param x the vectors position on the x axis.
     * @return fluent
     */
    public Vector setX(float x) {
        this.x = x;
        this.dirty = true;
        return this;
    }

    /**
     * @return the vectors position on the y axis.
     */
    public float getY() {
        return y;
    }

    /**
     * @param y the vectors position on the x axis.
     * @return fluent
     */
    public Vector setY(float y) {
        this.y = y;
        this.dirty = true;
        return this;
    }

    /**
     * @return the acceleration of the vector.
     */
    public float getAcceleration() {
        return acceleration;
    }

    /**
     * @param acceleration the acceleration of the vector.
     * @return fluent
     */
    public Vector setAcceleration(float acceleration) {
        this.acceleration = acceleration;
        return this;
    }

    /**
     * @return the direction the vector is moving in.
     */
    public float getDirection() {
        return direction;
    }

    /**
     * @param direction the new direction the vector is to move in.
     * @return fluent
     */
    public Vector setDirection(float direction) {
        this.direction = direction;
        return this;
    }

    /**
     * @return the velocity at which the vector moves.
     */
    public float getVelocity() {
        return velocity;
    }

    /**
     * @param velocity the new velocity at which the vector will be moved.
     * @return fluent
     */
    public Vector setVelocity(float velocity) {
        this.velocity = velocity;

        if (velocity == 0f) {
            acceleration = Vector.ACCELERATION_BASE;
        }

        return this;
    }

    /**
     * @return the virtual size of the vector from it's base point.
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the new virtual size of the vector.
     * @return fluent
     */
    public Vector setSize(int size) {
        this.size = size;
        return this;
    }

    /**
     * stops the vector by setting it's velocity to 0. same as calling {@link #setVelocity(float)} with 0.
     *
     * @return fluent
     */
    public Vector stop() {
        setVelocity(0f);
        return this;
    }

    /**
     * @return true if the velocity of the vector is greater than 0.
     */
    @JsonIgnore
    public boolean isMoving() {
        return velocity > 0f;
    }

    /**
     * @param another the other vector to check against
     * @return true if the current vector is to the left of another.
     */
    public boolean toLeftOf(Vector another) {
        return x < another.getX();
    }

    /**
     * @param another the other vector to check against.
     * @return true if the current vector is on top of another.
     */
    public boolean onTopOf(Vector another) {
        return y < another.getY();
    }

    /**
     * calculates the distance from the vectors position to the target coordinates.
     *
     * @param targetX the target x coordinate.
     * @param targetY the target y coordinate.
     * @return the distance between the given points.
     */
    public int distance(float targetX, float targetY) {
        return (int) Math.hypot(Math.abs(x - targetX), Math.abs(y - targetY));
    }

    /**
     * calculates the distance from the vectors position to the target coordinates.
     *
     * @param vector the target vector to check the distance to.
     * @return the distance between the given vectors.
     */
    public int distance(Vector vector) {
        return distance(vector.getX(), vector.getY());
    }

    /**
     * calculates the angle from the vectors point to the given target coordinates.
     *
     * @param targetX the target x coordinate.
     * @param targetY the target y coordinate.
     * @return the angle between the given points in radians.
     */
    public float targetAngle(float targetX, float targetY) {
        float theta = (float) (Math.atan2(y - targetY, targetX - x));
        theta += Math.toRadians(90);
        return theta;
    }

    @Override
    public String toString() {
        return String.format("x=%f y=%f, dir=%f velocity=%f", x, y, direction, velocity);
    }

    /**
     * @return a copy of this vector.
     */
    public Vector copy() {
        return new Vector()
                .setAcceleration(acceleration)
                .setVelocity(velocity)
                .setDirection(direction)
                .setSize(size)
                .setX(x).setY(y);
    }

    /**
     * Returns the cells that the vector is placed in.
     *
     * @param cellSize the size of the cells.
     * @param width the total width of the grid.
     * @return cell numbers that this vector exists within.
     */
    public Collection<Integer> cells(final int cellSize, final int width) {
        if (dirty) {
            dirty = false;
            Collection<Integer> cells = new HashSet<>();

            int cellX = (int) (x + width / 2) / cellSize;
            int cellY = (int) y / cellSize;
            int current = cellX + cellY * (width / cellSize);

            // check if vector moved from one cell to another.
            if (index != current) {
                index = current;

                cells.add(current);
                cells.add(current - 1); // left
                cells.add(current + 2); // right

                // tbd: size not taken account of, entities should
                // probably not be larger than the cell size.

                int row = (width / cellSize);
                cells.add(current - row - 1);
                cells.add(current - row);
                cells.add(current - row + 1);
                cells.add(current + row - 1);
                cells.add(current + row);
                cells.add(current + row + 1);

                // memory address changed, triggers a grid update.
                buckets = cells;
            }
        }
        return buckets;
    }

    /**
     * @param ticker Moves the vector in its direction given its velocity.
     */
    public void forward(Ticker ticker) {
        if (velocity > 0) {
            if (acceleration < 1) {
                acceleration += ACCELERATION_STEP * (ticker.delta() / ACCELERATION_TIME);
            } else {
                acceleration = 1.0f;
            }

            x += Math.sin(direction) * (velocity * acceleration * ticker.delta());
            y += Math.cos(direction) * (velocity * acceleration * ticker.delta());

            dirty = true;
        }
    }
}
