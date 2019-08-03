package com.codingchili.instance.model.items;

import com.codingchili.instance.model.stats.Stats;

import java.util.UUID;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 *
 * Model of an item, which may be usable, equippable of consumable.
 */
public class Item extends ItemType implements Storable {
    private String id = UUID.randomUUID().toString();
    protected String icon = "dagger.png";
    protected String name = "no name";
    protected String description = "no description.";
    protected Stats stats = new Stats();
    protected ItemRarity rarity = ItemRarity.common;
    protected Integer quantity = 1;
    protected String onDamaged = null;
    protected String onHit = null;
    protected String onUse = null;

    public String getDescription() {
        return description;
    }

    public Item setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getName() {
        return name;
    }

    public Item setName(String name) {
        this.name = name;
        return this;
    }

    public Stats getStats() {
        return stats;
    }

    public Item setStats(Stats stats) {
        this.stats = stats;
        return this;
    }

    public String getOnHit() {
        return onHit;
    }

    public Item setOnHit(String onHit) {
        this.onHit = onHit;
        return this;
    }

    public String getOnDamaged() {
        return onDamaged;
    }

    public String getOnUse() {
        return onUse;
    }

    public Item setOnUse(String onUse) {
        this.onUse = onUse;
        return this;
    }

    public Item setOnDamaged(String onDamaged) {
        this.onDamaged = onDamaged;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Item setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
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

    public Item setRarity(ItemRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public Item setIcon(String icon) {
        this.icon = icon;
        return this;
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
