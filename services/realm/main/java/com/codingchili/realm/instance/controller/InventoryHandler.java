package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.entity.EventProtocol;
import com.codingchili.realm.instance.model.events.*;
import com.codingchili.realm.instance.model.items.InventoryEngine;
import com.codingchili.realm.instance.transport.InstanceRequest;

import java.util.UUID;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 *
 * Handles inventory - list, equip and consume items.
 */
public class InventoryHandler implements GameHandler {
    private InventoryEngine inventory;
    private GameContext game;

    /**
     * @param game the game context that the inventory handler is run on.
     */
    public InventoryHandler(GameContext game) {
        this.inventory = game.inventory();
        this.game = game;

        // when a creature dies: spawn its loot as a lootable entity.
        EventProtocol events = new EventProtocol();
        events.use(EventType.death.name(), this::onDeathHandler);
        game.subscribe(UUID.randomUUID().toString(), events);
    }

    private void onDeathHandler(Event event) {
        DeathEvent death = (DeathEvent) event;
        inventory.spawnLoot(game.getById(death.getTargetId()));
    }


    @Api(route = "equip_item")
    public void equip(InstanceRequest request) {
        inventory.equip(creature(request), request.raw(EquipItemEvent.class).getItemId());
    }

    @Api(route = "unequip_item")
    public void unequip(InstanceRequest request) {
        inventory.unequip(creature(request), request.raw(UnequipItemEvent.class).getSlot());
    }

    @Api(route = "use_item")
    public void use(InstanceRequest request) {
        UseItemEvent event = request.raw(UseItemEvent.class);
        inventory.use(creature(request), event.getTarget(), event.getItemId());
    }

    @Api(route = "drop_item")
    public void drop(InstanceRequest request) {
        inventory.drop(creature(request), request.raw(DropItemEvent.class).getItemId());
    }

    @Api(route = "loot_items")
    public void loot(InstanceRequest request) {
        LootEntityEvent event = request.raw(LootEntityEvent.class);
        inventory.takeLoot(creature(request), event.getTargetId(), event.getItemId());
    }

    @Api(route = "loot_list")
    public void listLoot(InstanceRequest request) {
        ListEntityLootEvent event = request.raw(ListEntityLootEvent.class);
        inventory.listLoot(creature(request), event.getTargetId());
    }

    private Creature creature(InstanceRequest request) {
        return game.getById(request.target());
    }
}
