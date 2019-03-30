package com.codingchili.instance.model;

/**
 * @author Robin Duda
 *
 * A spawnpoint in an instance.
 */
public class SpawnPoint {
    private float probability = 1.0f;
    private int x;
    private int y;

    public float getProbability() {
        return probability;
    }

    public SpawnPoint setProbability(float probability) {
        this.probability = probability;
        return this;
    }

    public int getX() {
        return x;
    }

    public SpawnPoint setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public SpawnPoint setY(int y) {
        this.y = y;
        return this;
    }
}
