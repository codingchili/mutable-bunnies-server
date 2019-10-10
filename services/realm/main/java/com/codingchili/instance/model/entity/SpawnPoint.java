package com.codingchili.instance.model.entity;

/**
 * @author Robin Duda
 *
 * A spawnpoint in an instance, a point and a probability.
 */
public class SpawnPoint extends Point {
    private float probability = 1.0f;

    /**
     * @return the probability of choosing this spawn point.
     */
    public float getProbability() {
        return probability;
    }

    /**
     * @param probability the probability to choose this spawn point, from 0.0 to 1.0.
     * @return fluent
     */
    public SpawnPoint setProbability(float probability) {
        this.probability = probability;
        return this;
    }
}
