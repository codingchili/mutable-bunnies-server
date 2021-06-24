package com.codingchili.instance.model.items;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.dialog.InteractionOutOfRangeException;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.events.NotificationEvent;
import com.codingchili.instance.model.npc.LootableEntity;
import com.codingchili.instance.model.spells.SpellTarget;
import com.codingchili.instance.scripting.*;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Implements logic for modifying creature inventory.
 */
public class InventoryEngine {
    private static final String TARGET = "target";
    private static final int LOOT_RANGE = 164;
    private static final int ITEM_USE_GCD = 1000;
    private static final String ITEM = "item";
    private static final String SPELLS = "spells";
    private static final String SKILLS = "skills";
    private static final String SUCCESS = "success";
    private static final String FAIL = "fail";
    private ItemDB items;
    private GameContext game;

    /**
     * @param game the game context to run on.
     */
    public InventoryEngine(GameContext game) {
        this.game = game;
        this.items = new ItemDB(game.instance());
    }

    /**
     * Changes the amount of currency the given creature holds.
     *
     * @param target the target to modify the currency value of.
     * @param amount the amount to modify with, can be negative.
     * @return true if there was enough currency left to withdraw.
     */
    public boolean currency(Creature target, int amount) {
        Inventory inventory = target.getInventory();
        int current = inventory.getCurrency();

        if (current + amount < 0) {
            return false;
        } else {
            inventory.setCurrency(current + amount);
            target.handle(new InventoryUpdateEvent(target));
            return true;
        }
    }

    /**
     * Adds the given item to the targets inventory if the item exists
     * in the database.
     *
     * @param target the creature of which inventory to add the item to.
     * @param itemId the id of the item to add.
     * @param amount the quantity of the item to add.
     * @return true if the item was found and added to the targets inventory.
     */
    public boolean item(Creature target, String itemId, int amount) {
        Optional<Item> item = this.items.getById(itemId);

        item.ifPresent(found -> {
            found.setQuantity(amount);
            target.getInventory().add(found);
            target.handle(new InventoryUpdateEvent(target));
        });
        return item.isPresent();
    }

    /**
     * Adds the given item to the players inventory.
     *
     * @param target the target to add the item to.
     * @param item   the item to add.
     */
    public void item(Creature target, Item item) {
        target.getInventory().add(item);
        target.handle(new InventoryUpdateEvent(target));
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
        game.publish(new StatsUpdateEvent(source));
        game.publish(new UnequipItemEvent(source, slot));
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
        Slot slot = item.getSlot();

        if (!item.getSlot().equals(Slot.none)) {
            int position = inventory.getItems().indexOf(item);
            inventory.getItems().remove(item);

            if (inventory.getEquipped().containsKey(item.getSlot())) {
                // if slot is weapon and offhand is free, equip to offhand.
                if (Slot.weapon == item.getSlot() && !inventory.getEquipped().containsKey(Slot.offhand)) {
                    inventory.getEquipped().put(Slot.offhand, item);
                    slot = Slot.offhand;
                } else {
                    inventory.getItems().add(position,
                            inventory.getEquipped().replace(item.getSlot(), item));
                }
            } else {
                inventory.getEquipped().put(item.slot, item);
            }
            update(source);

            game.publish(new StatsUpdateEvent(source));
            game.publish(new EquipItemEvent(source, item, slot));
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

        if (source.getSpells().isOnGCD()) {
            // notify client?
        } else {
            // trigger gcd first to prevent race conditions/multi-use.
            source.getSpells().triggerGcd(ITEM_USE_GCD);

            if (item.getOnUse() != null) {
                Scripted scripted = new ScriptReference(item.onUse);
                var failed = new AtomicBoolean(false);
                var called = new AtomicBoolean(false);

                // show notification banner on error.
                var fail = (Consumer<String>) (message) -> {
                    source.handle(new NotificationEvent(message));
                    failed.set(true);
                    called.set(true);
                };

                // consume the item on success.
                var success = (Runnable) () -> {
                    item.setQuantity(item.getQuantity() - 1);

                    if (item.getQuantity() < 1) {
                        inventory.getItems().remove(item);
                    }
                    called.set(true);
                };

                Bindings bindings = new Bindings();
                bindings.setContext(game)
                        .set(ITEM, item)
                        .set(SUCCESS, success)
                        .set(FAIL, fail)
                        .set(SPELLS, game.spells())
                        .set(SKILLS, game.skills())
                        .setSource(source)
                        .set(TARGET, target);

                scripted.apply(bindings);

                if (!called.get()) {
                    // assume success if fail is not called - prefer to consume item without effects.
                    success.run();
                }

                update(source);
            }
        }
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
        game.add(LootableEntity.dropped(source.getVector(), item));
    }

    /**
     * Drops an item in the world at the given location.
     *
     * @param vector the target point to drop the item at.
     * @param item   the item to drop.
     */
    public void drop(Vector vector, Item item) {
        game.add(LootableEntity.dropped(vector, item));
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

        // drop everything equipped and in inventory.
        inventory.getEquipped().clear();
        inventory.getItems().clear();

        game.instance().save(source);
        game.add(LootableEntity.fromCorpse(source, loot));
    }

    /**
     * Takes an item out of the loot container as specified by its item id
     * and moves it into the inventory of the looter. If the source entity
     * is not already subscribed the action is ignored.
     *
     * @param source   the creature performing the looting.
     * @param targetId the id of the container that holds the loot.
     * @param itemId   the id of the item in the container to take.
     */
    public void takeLoot(Creature source, String targetId, String itemId) {
        LootableEntity entity = game.getById(targetId);

        if (entity.subscribed(source)) {
            Item item = entity.takeItem(itemId);
            source.getInventory().add(item);
            update(source);
        }
    }

    /**
     * Takes all items out of the loot container and moves it into the
     * inventory of the looter. No subscription is required.
     *
     * @param source the creature performing the looting.
     * @param targetId the if of the container that holds the loot.
     */
    public Future<Void> takeAll(Creature source, String targetId) {
        if (targetInRange(source, game.getById(targetId))) {
            LootableEntity entity = game.getById(targetId);
            source.getInventory().addAll(entity.takeAll());
            update(source);
            return Future.succeededFuture();
        } else {
            return Future.failedFuture(new InteractionOutOfRangeException());
        }
    }

    /**
     * Lists available loot in the given container.
     *
     * @param source   the creature that is interested in the loot contents.
     * @param targetId the target loot container.
     * @return future
     */
    public Future<Void> listLoot(Creature source, String targetId) {
        LootableEntity entity = game.getById(targetId);
        Promise<Void> promise = Promise.promise();

        if (targetInRange(source, entity)) {
            if (entity.getItems().isEmpty()) {
                promise.fail(new LootableEmptyException());
            } else {
                entity.subscribe(source);
                promise.complete();
                game.movement().stop(source);
            }
        } else {
            promise.fail(new InteractionOutOfRangeException());
        }
        return promise.future();
    }

    /**
     * Called after subscribing to a loot container to not receive any more update events.
     *
     * @param target     the unsubscribing entity.
     * @param subscribed the lootable entity.
     */
    public void unsubscribe(String target, String subscribed) {
        LootableEntity entity = game.getById(subscribed);
        entity.unsubscribe(target);
    }

    private boolean targetInRange(Entity source, Entity target) {
        int distance = source.getVector().distance(target.getVector());
        return distance < LOOT_RANGE;
    }

    private void update(Creature source) {
        source.getInventory().update();
        source.handle(new InventoryUpdateEvent(source));
    }

    public ItemDB items() {
        return items;
    }
}
