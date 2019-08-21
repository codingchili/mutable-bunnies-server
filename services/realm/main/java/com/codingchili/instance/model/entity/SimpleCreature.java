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

    @Override
    public boolean isDead() {
        return stats.getOrDefault(Attribute.health, 0.0) < 1;
    }

    protected void onStatsModifier(Stats calculated) {
    }

    public Stats getStats() {
        if (inventory.getStats().isDirty() || afflictions.getStats().isDirty() || stats.isDirty()) {
            calculated.clear();

            onStatsModifier(calculated);

            calculated.apply(inventory.getStats())
                    .apply(afflictions.getStats())
                    .apply(stats)
                    .set(Attribute.maxhealth, calculateMaxHealth(calculated))
                    .set(Attribute.maxenergy, calculateMaxEnergy(calculated));

            if (!stats.has(Attribute.energy)) {
                stats.set(Attribute.energy, calculated.get(Attribute.maxenergy));
            }

            if (!stats.has(Attribute.health)) {
                stats.set(Attribute.health, calculated.get(Attribute.maxhealth));
            }

            if (!calculated.has(Attribute.level)) {
                stats.setDefault(Attribute.level, 1);
            }

            stats.set(Attribute.health,
                    Math.min(stats.get(Attribute.health), calculated.get(Attribute.maxhealth)));

            stats.set(Attribute.energy,
                    Math.min(stats.get(Attribute.energy), calculated.get(Attribute.maxenergy)));

            inventory.getStats().clean();
            afflictions.getStats().clean();
            stats.clean();
        }
        return calculated;
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
        this.stats = stats;
    }
}
