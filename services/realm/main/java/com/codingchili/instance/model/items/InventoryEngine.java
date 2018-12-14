package com.codingchili.instance.model.items;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.npc.LootableEntity;
import com.codingchili.instance.model.events.EquipItemEvent;
import com.codingchili.instance.model.spells.SpellTarget;
import com.codingchili.instance.scripting.Bindings;

import java.util.ArrayList;
import java.util.Collections;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Implements logic for modifying creature inventory.
 */
public class InventoryEngine {
    public static final String TARGET = "target";
    private GameContext game;

    /**
     * @param game the game context to run on.
     */
    public InventoryEngine(GameContext game) {
        this.game = game;
    }

    /**
     * Removes an equipped item and places it into the creatures bag.
     *
     * @param source the creature to unequip an item..
     * @param slot   the slot of the item to unequip.
     */
    public void unequip(Creature source, Slot slot) {
        Inventory inventory = source.getInventory();
        Item item = inventory.getEquipped().remove(slot);
        if (item != null) {
            inventory.getItems().add(item);
        }
        update(source);
    }

    /**
     * equips the given item on the specified creature.
     *
     * @param source the creature to equip an item
     * @param itemId the id of the item in the creatures inventory.
     */
    public void equip(Creature source, String itemId) {
        Inventory inventory = source.getInventory();
        Item item = inventory.getById(itemId);

        if (!item.getSlot().equals(Slot.none)) {

            // remove item from pack.
            inventory.getItems().remove(item);

            if (inventory.getEquipped().containsKey(item.getSlot())) {
                // slot already equipped: move to pack.
                inventory.getItems().add(
                        inventory.getEquipped().replace(item.getSlot(), item));
            } else {
                // slot not already equipped.
                inventory.getEquipped().put(item.slot, item);
            }
            update(source);

            // publish this so players can render equipped items.
            game.publish(new EquipItemEvent()
                    .setItemId(itemId)
                    .setSource(source.getId()));
        } else {
            throw new CoreRuntimeException("Not able to equip item: " + item.getName());
        }
    }

    /**
     * Uses an item in the inventory, reducing its quantity by 1 and applying effects.
     *
     * @param source the creature that uses the item.
     * @param target the target of the item use.
     * @param itemId the id of the item in the sources inventory.
     */
    public void use(Creature source, SpellTarget target, String itemId) {
        Inventory inventory = source.getInventory();
        Item item = inventory.getById(itemId);

        if (item.isUsable()) {
            item.setQuantity(item.getQuantity() - 1);

            if (item.getQuantity() < 1) {
                inventory.getItems().remove(item);
            }

            Bindings bindings = new Bindings();
            bindings.setContext(game)
                    .setSource(source)
                    .set(TARGET, target);

            item.onUse.apply(bindings);
        }
        update(source);
    }

    /**
     * Drops an item out of the specified creatures inventory.
     *
     * @param source the specified creature to drop an item from.
     * @param itemId the id of the item to drop.
     */
    public void drop(Creature source, String itemId) {
        Inventory inventory = source.getInventory();
        Item item = inventory.getById(itemId);
        inventory.getItems().remove(item);
        update(source);
        game.add(new LootableEntity(source.getVector(), Collections.singletonList(item)));
    }

    /**
     * Spawns loot from a creature upon death.
     *
     * @param source the creature to spawn loot from.
     */
    public void spawnLoot(Creature source) {
        Inventory inventory = source.getInventory();

        ArrayList<Item> loot = new ArrayList<>();
        loot.addAll(inventory.getItems());
        loot.addAll(inventory.getEquipped().values());

        inventory.getEquipped().clear();
        inventory.getItems().clear();
        update(source);

        game.add(new LootableEntity(source.getVector(), loot));
    }

    /**
     * Takes an item out of the loot container as specified by its item id
     * and moves it into the inventory of the looter.
     *
     * @param source   the creature performing the looting.
     * @param targetId the id of the container that holds the loot.
     * @param itemId   the id of the item in the container to take.
     */
    public void takeLoot(Creature source, String targetId, String itemId) {
        LootableEntity entity = game.getById(targetId);
        Item item = entity.takeItem(itemId);
        source.getInventory().add(item);
        update(source);
    }

    /**
     * Lists available loot in the given container.
     *
     * @param source   the creature that is interested in the loot contents.
     * @param targetId the target loot container.
     */
    public void listLoot(Creature source, String targetId) {
        LootableEntity entity = game.getById(targetId);

        // must subscribe to updates when other players remove loot etc.
        entity.subscribe(source);
    }

    private void update(Creature source) {
        // new stats: propagate?
        source.getInventory().update();
    }
}
