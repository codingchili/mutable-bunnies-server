package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Represents a characters inventory.
 */
public class Inventory implements Serializable {
    public static Inventory EMPTY = new Inventory();

    private Map<Slot, Item> equipped = new ConcurrentHashMap<>();
    private Set<Item> items = new HashSet<>();
    private Stats stats = new Stats();
    private Integer space;
    private Integer currency = 1;

    /**
     * Updates the equipped stats after changing equipped items.
     */
    public void update() {
        stats.clear();
        equipped.forEach((slot, equipped) -> stats.apply(equipped.getStats()));
    }

    /**
     * @param id the id of the item to retrieve.
     * @return the item if found, throws if not found.
     */
    public Item getById(String id) {
        for (Item item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        throw new CoreRuntimeException(String.format("Item with id '%d' does not exist.", id));
    }

    public void add(Item item) {
        items.add(item);
    }

    public Integer getCurrency() {
        return currency;
    }

    public void setCurrency(Integer currency) {
        this.currency = currency;
    }

    public Map<Slot, Item> getEquipped() {
        return equipped;
    }

    public void setEquipped(Map<Slot, Item> equipped) {
        this.equipped = equipped;
    }

    public Integer getSpace() {
        return space;
    }

    public void setSpace(Integer space) {
        this.space = space;
    }

    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    @JsonIgnore
    public Stats getStats() {
        return stats;
    }
}
