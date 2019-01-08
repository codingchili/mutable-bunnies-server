package com.codingchili.instance.model.npc;

/**
 * @author Robin Duda
 *
 * Describes the probability of an item being dropped.
 */
public class LootTableItem {
    private String item;
    private float probability = 1.0f;
    private int min = 1;
    private int max = 1;

    public String getItem() {
        return item;
    }

    public LootTableItem setItem(String item) {
        this.item = item;
        return this;
    }

    public float getProbability() {
        return probability;
    }

    public LootTableItem setProbability(float probability) {
        this.probability = probability;
        return this;
    }

    public int getMin() {
        return min;
    }

    public LootTableItem setMin(int min) {
        this.min = min;
        return this;
    }

    public int getMax() {
        return max;
    }

    public LootTableItem setMax(int max) {
        this.max = max;
        return this;
    }
}
