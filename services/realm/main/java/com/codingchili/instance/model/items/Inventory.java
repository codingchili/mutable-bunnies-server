package com.codingchili.instance.model.items;

import com.codingchili.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Represents a characters inventory.
 */
public class Inventory implements Serializable {
    private Map<Slot, Item> equipped = new LinkedHashMap<>();
    private List<Item> items = new ArrayList<>();
    private transient Stats stats;
    private Integer space;
    private Integer currency = 1000;

    /**
     * Updates the equipped stats after changing equipped items.
     */
    public void update() {
        if (stats == null) {
            stats = new Stats();
        }
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
        throw new CoreRuntimeException(String.format("Item with id '%s' does not exist.", id));
    }

    public Inventory add(Item item) {
        if (items.contains(item)) {
            items.forEach(entry -> {
                if (entry.getId().equals(item.getId())) {
                    entry.setQuantity(entry.getQuantity() + item.quantity);
                }
            });
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
        // make sure to compute stats after deserialization.
        this.equipped = equipped;
    }

    public Integer getSpace() {
        return space;
    }

    public void setSpace(Integer space) {
        this.space = space;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @JsonIgnore
    public Stats getStats() {
        if (stats == null) {
            update();
        }
        return stats;
    }
}
