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

    public void setProbability(float probability) {
        this.probability = probability;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
