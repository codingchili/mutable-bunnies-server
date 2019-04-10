package com.codingchili.instance.controller;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.EventProtocol;
import com.codingchili.instance.model.events.*;
import com.codingchili.instance.model.items.*;
import com.codingchili.instance.model.spells.DeathEvent;
import com.codingchili.instance.transport.InstanceRequest;

import java.util.UUID;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 * <p>
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

    @Api
    public void equip_item(InstanceRequest request) {
        inventory.equip(creature(request), request.raw(EquipItemEvent.class).getItemId());
    }

    @Api
    public void unequip_item(InstanceRequest request) {
        inventory.unequip(creature(request), request.raw(UnequipItemEvent.class).getSlot());
    }

    @Api
    public void use_item(InstanceRequest request) {
        UseItemEvent event = request.raw(UseItemEvent.class);
        inventory.use(creature(request), event.getTarget(), event.getItemId());
    }

    @Api
    public void drop_item(InstanceRequest request) {
        inventory.drop(creature(request), request.raw(DropItemEvent.class).getItemId());
    }

    @Api
    public void loot_item(InstanceRequest request) {
        LootEntityEvent event = request.raw(LootEntityEvent.class);
        inventory.takeLoot(creature(request), event.getTargetId(), event.getItemId());
    }

    @Api
    public void loot_list(InstanceRequest request) {
        ListEntityLootEvent event = request.raw(ListEntityLootEvent.class);
        inventory.listLoot(creature(request), event.getTargetId()).setHandler(done -> {
            if (done.failed()) {
                request.error(done.cause());
            }
        });
    }

    @Api
    public void loot_unsubscribe(InstanceRequest request) {
        LootUnsubscribeEvent event = request.raw(LootUnsubscribeEvent.class);
        inventory.unsubscribe(request.target(), event.getEntityId());
    }

    private Creature creature(InstanceRequest request) {
        return game.getById(request.target());
    }
}
