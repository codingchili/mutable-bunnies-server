package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.model.stats.Stats;
import com.codingchili.realm.instance.scripting.Scripted;

import java.util.UUID;

/**
 * @author Robin Duda
 *
 * Model of an item, which may be usable, equippable of consumable.
 */
public class Item extends ItemType {
    private String id = UUID.randomUUID().toString();
    protected String name = "no name";
    protected String description = "no description.";
    protected Stats stats = new Stats();
    protected ItemRarity rarity = ItemRarity.COMMON;
    protected Scripted onDamaged = null;
    protected Scripted onHit = null;
    protected Integer quantity = 1;

    // todo: needs a cooldown?
    protected Scripted onUse = null;

    public Boolean isUsable() {
        return (onUse != null);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Scripted getOnHit() {
        return onHit;
    }

    public void setOnHit(Scripted onHit) {
        this.onHit = onHit;
    }

    public Scripted getOnDamaged() {
        return onDamaged;
    }

    public void setOnDamaged(Scripted onDamaged) {
        this.onDamaged = onDamaged;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public Item setId(String id) {
        this.id = id;
        return this;
    }

    public ItemRarity getRarity() {
        return rarity;
    }

    public void setRarity(ItemRarity rarity) {
        this.rarity = rarity;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Item) &&
                ((Item) (obj)).getId().equals(id);
    }
}
