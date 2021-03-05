package com.codingchili.instance.model.items;

import com.codingchili.core.protocol.Serializer;

import com.codingchili.instance.model.entity.Model;
import com.codingchili.instance.model.stats.*;

import java.util.UUID;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * Model of an item, which may be usable, equippable of consumable.
 */
public class Item extends ItemType implements Storable {
    private String id = UUID.randomUUID().toString();
    private Model model = null;
    protected String icon = "dagger.png";
    protected String name = "no name";
    protected String description = "no description.";
    protected Stats stats = new Stats();
    protected ItemRarity rarity = ItemRarity.common;
    protected Integer quantity = 1;
    protected String onDamaged = null;
    protected String onAttack = null;
    protected String onUse = null;

    public Boolean isConsumable() {
        return onUse != null;
    }

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

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Stats getStats() {
        return stats;
    }

    public Item setStats(Stats stats) {
        this.stats = stats;
        return this;
    }

    public String getOnAttack() {
        return onAttack;
    }

    public Item setOnAttack(String onAttack) {
        this.onAttack = onAttack;
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

    public static void main(String[] args) {
        Item item = new Item() {{
            description = "once upon a time item description";
            rarity = ItemRarity.epic;
            slot = Slot.ring;
            armorType = ArmorType.plate;
            weaponType = WeaponType.battleaxe;
            name = "Bottlax of Killah";
            onUse = "slurp.groovy";
            onAttack = "boom.groovy";
            onDamaged = "zap.groovy";
            icon = "dagger.png";
            quantity = 99;
        }};
        System.out.println(Serializer.json(item));
    }
}
