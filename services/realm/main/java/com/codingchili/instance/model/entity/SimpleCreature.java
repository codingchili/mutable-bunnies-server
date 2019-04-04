package com.codingchili.instance.model.entity;

import com.codingchili.instance.model.afflictions.AfflictionState;
import com.codingchili.instance.model.items.Inventory;
import com.codingchili.instance.model.spells.SpellState;
import com.codingchili.instance.model.stats.Stats;

/**
 * @author Robin Duda
 *
 * basic class for creatures.
 */
public abstract class SimpleCreature extends SimpleEntity implements Creature {
    private transient Stats calculated = new Stats();
    protected transient AfflictionState afflictions = new AfflictionState();
    protected Inventory inventory = new Inventory();
    protected SpellState spells = new SpellState();
    protected Stats stats = new Stats();

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public AfflictionState getAfflictions() {
        return afflictions;
    }

    @Override
    public Stats getBaseStats() {
        return stats;
    }

    @Override
    public SpellState getSpells() {
        return spells;
    }

    public Stats getStats() {
        calculated.clear();
        return calculated
                .apply(inventory.getStats())
                .apply(afflictions.getStats())
                .apply(stats);
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setAfflictions(AfflictionState afflictions) {
        this.afflictions = afflictions;
    }

    public void setSpells(SpellState spells) {
        this.spells = spells;
    }

    public void setBaseStats(Stats stats) {
        this.stats = stats;
    }
}
