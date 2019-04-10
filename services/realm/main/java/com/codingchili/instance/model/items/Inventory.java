package com.codingchili.instance.model.items;

import com.codingchili.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Represents a characters inventory.
 */
public class Inventory implements Serializable {
    public static Inventory EMPTY = new Inventory();

    private Map<Slot, Item> equipped = new ConcurrentHashMap<>();
    private Collection<Item> items = new ArrayList<>();
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

    public Inventory add(Item item) {
        if (items.contains(item)) {
            items = items.stream()
                    .filter(existing -> existing.getId().equals(item.getId()))
                    .map(existing -> existing.setQuantity(existing.quantity + item.quantity))
                    .collect(Collectors.toList());
        } else {
            items.add(item);
        }
        return this;
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

    public Collection<Item> getItems() {
        return items;
    }

    public void setItems(Collection<Item> items) {
        this.items = items;
    }

    @JsonIgnore
    public Stats getStats() {
        return stats;
    }
}
