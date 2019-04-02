package com.codingchili.instance.model.entity;

import com.codingchili.instance.context.GameContext;

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
    private static final float ACCELERATION_STEP = (1 - ACCELERATION_BASE) / GameContext.secondsToTicks(0.5);
    private transient Collection<Integer> buckets = new HashSet<>();
    private transient float acceleration = ACCELERATION_BASE;
    private transient boolean dirty = false;
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
    public float targetDistance(float targetX, float targetY) {
        return (float) Math.hypot(Math.abs(x - targetX), Math.abs(y - targetY));
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
     * @return cell numbers that this vector exists within.
     */
    public Collection<Integer> cells(final int cellSize) {
        if (dirty) {
            dirty = false;
            Collection<Integer> cells = new HashSet<>();

            int cellCount = (size * 2) / cellSize;
            int cellY = (int) y / cellSize;
            int cellX = (int) x / cellSize;

            cells.add(cellY * cellSize + cellX);

            for (int y = cellY; y < cellCount + cellY; y++) {
                for (int x = cellX; x < cellCount + cellX; x++) {
                    cells.add(((x - cellCount / 2) + (y - cellCount / 2) * cellSize));
                }
            }
            buckets = cells;
        }
        return buckets;
    }

    /**
     * @param delta Moves the vector in its direction given its velocity.
     */
    public void forward(float delta) {
        if (velocity > 0) {
            if (acceleration < 1) {
                acceleration += ACCELERATION_STEP * delta;
            } else {
                acceleration = 1.0f;
            }

            x += Math.sin(direction) * (velocity * acceleration * delta);
            y += Math.cos(direction) * (velocity * acceleration * delta);

            dirty = true;
        }
    }
}
