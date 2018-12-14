package com.codingchili.instance.controller;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.items.Item;

import java.util.Collection;

/**
 * @author Robin Duda
 */
public class TradeHandler implements GameHandler {
    private Creature initiator;
    private Creature other;
    private Collection<Item> initiatorItems;
    private Collection<Item> otherItems;
    private GameContext game;

    public TradeHandler(GameContext game) {
        this.game = game;
    }

    public void offer(Creature creature, Item item) {
        // notify other entity of added item
    }

    public void remove(Creature creature, Item item) {
        // notify other entity of removed item.
    }

    public void accept(Creature creature) {
        // both entities accept the trade.
        // transfer items here, lock entities inventories.
    }

    public void complete(Creature creature) {
        // both entities completes the trade.
    }
}
