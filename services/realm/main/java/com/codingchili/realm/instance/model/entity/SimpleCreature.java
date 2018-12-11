package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.model.afflictions.AfflictionState;
import com.codingchili.realm.instance.model.items.Inventory;
import com.codingchili.realm.instance.model.spells.SpellState;
import com.codingchili.realm.instance.model.stats.Attribute;
import com.codingchili.realm.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 *
 * basic class for creatures.
 */
public abstract class SimpleCreature extends SimpleEntity implements Creature {
    private transient Stats calculated = new Stats();
    protected transient AfflictionState afflictions = new AfflictionState();
    protected transient Stats stats = new Stats();
    protected Inventory inventory = new Inventory();
    protected SpellState spells = new SpellState();

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public AfflictionState getAfflictions() {
        return afflictions;
    }

    @Override
    @JsonIgnore
    public Stats getBaseStats() {
        return stats;
    }

    @Override
    public SpellState getSpells() {
        return spells;
    }

    public Stats getStats() {
        calculated.clear();
        Stats current = calculated.apply(inventory.getStats()).apply(afflictions.getStats()).apply(stats);

        // these depend on the current stats.
        current.set(Attribute.maxhealth, current.get(Attribute.constitution) * 10);
        current.set(Attribute.maxenergy, current.get(Attribute.dexterity) * 20);

        return current;
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
