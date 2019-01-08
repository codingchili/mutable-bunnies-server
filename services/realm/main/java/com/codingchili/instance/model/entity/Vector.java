package com.codingchili.instance.model.entity;

import java.util.*;

import com.codingchili.instance.context.GameContext;

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
    private transient List<Integer> buckets = new ArrayList<>();
    private transient float acceleration = 1.0f;
    private transient boolean dirty = false;
    private float velocity = 0.0f;
    private float direction = 0.0f;
    private int size = 24;
    private float x = -1;
    private float y = -1;

    public float getX() {
        return x;
    }

    public Vector setX(float x) {
        this.x = x;
        this.dirty = true;
        return this;
    }

    public float getY() {
        return y;
    }

    public Vector setY(float y) {
        this.y = y;
        this.dirty = true;
        return this;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public Vector setAcceleration(float acceleration) {
        this.acceleration = acceleration;
        return this;
    }

    public float getDirection() {
        return direction;
    }

    public Vector setDirection(float direction) {
        this.direction = direction;
        return this;
    }

    public float getVelocity() {
        return velocity;
    }

    public Vector setVelocity(float velocity) {
        this.velocity = velocity;
        return this;
    }

    public int getSize() {
        return size;
    }

    public Vector setSize(int size) {
        this.size = size;
        return this;
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
     * @param cellSize  the size of the cells.
     * @return cell numbers that this vector exists within.
     */
    public Collection<Integer> cells(final int cellSize) {
        if (velocity > 0 || dirty) {
            dirty = false;
            List<Integer> cells = new ArrayList<>();
            cells.add(Math.round(((x + size) / cellSize) + ((y / cellSize))));
            cells.add(Math.round(((x - size) / cellSize) + ((y / cellSize))));
            cells.add(Math.round((x / cellSize) + (((y + size) / cellSize))));
            cells.add(Math.round((x / cellSize) + (((y - size) / cellSize))));
            buckets = cells;
        }
        // todo: handle cells that cover more than two cells! if cellSize < size perform some loop.
        return buckets;
    }

    /**
     * Moves the vector in its direction given its velocity.
     */
    public void forward() {

        if (acceleration < 1) {
            acceleration += ACCELERATION_STEP;
        } else {
            acceleration = 1.0f;
        }

        x += Math.sin(direction) * (velocity * acceleration);
        y += Math.cos(direction) * (velocity * acceleration);
    }
}
