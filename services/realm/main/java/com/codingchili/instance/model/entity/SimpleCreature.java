package com.codingchili.instance.model.entity;

import com.codingchili.instance.model.afflictions.AfflictionState;
import com.codingchili.instance.model.items.Inventory;
import com.codingchili.instance.model.spells.SpellState;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.model.stats.Stats;

/**
 * @author Robin Duda
 * <p>
 * basic class for creatures.
 */
public abstract class SimpleCreature extends SimpleEntity implements Creature {
    protected AfflictionState afflictions = new AfflictionState();
    private transient Stats calculated = new Stats();
    protected Inventory inventory = new Inventory();
    protected SpellState spells = new SpellState();
    protected Stats baseStats = new Stats();

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
        return baseStats;
    }

    @Override
    public SpellState getSpells() {
        return spells;
    }

    @Override
    public boolean isCreature() {
        return true;
    }

    @Override
    public boolean isDead() {
        return baseStats.getOrDefault(Attribute.health, 0.0) < 1;
    }

    protected boolean onClassModifier(Stats calculated) {
        return false;
    }

    public Stats getStats() {
        if (inventory.getStats().isDirty() || afflictions.getStats().isDirty() || baseStats.isDirty()) {
            compute();
        }
        return calculated;
    }

    public void compute() {
        calculated.clear();

        boolean modifier = onClassModifier(calculated);

        calculated.apply(inventory.getStats())
                .apply(afflictions.getStats())
                .apply(baseStats)
                .set(Attribute.maxhealth, calculateMaxHealth(calculated))
                .set(Attribute.maxenergy, calculateMaxEnergy(calculated));

        if (!baseStats.has(Attribute.energy)) {
            baseStats.set(Attribute.energy, calculated.get(Attribute.maxenergy));
        }

        if (!baseStats.has(Attribute.health)) {
            baseStats.set(Attribute.health, calculated.get(Attribute.maxhealth));
        }

        if (!calculated.has(Attribute.level)) {
            baseStats.setDefault(Attribute.level, 1);
        }

        if (modifier) {
            calculated.set(Attribute.health,
                    Math.min(baseStats.get(Attribute.health), calculated.get(Attribute.maxhealth)));

            calculated.set(Attribute.energy,
                    Math.min(baseStats.get(Attribute.energy), calculated.get(Attribute.maxenergy)));
        }

        inventory.getStats().clean();
        afflictions.getStats().clean();
        baseStats.clean();
    }

    private Double calculateMaxEnergy(Stats stats) {
        return stats.get(Attribute.wisdom) * 10.0d +
                stats.get(Attribute.dexterity) * 5.0d +
                stats.get(Attribute.level) * 15.0d;
    }

    private Double calculateMaxHealth(Stats stats) {
        return stats.get(Attribute.constitution) * 10.0d +
                stats.get(Attribute.level) * 25.0d;
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
        this.baseStats = stats;
    }
}
