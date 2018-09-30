package com.codingchili.realm.instance.model.npc;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.*;
import com.codingchili.realm.instance.model.entity.Vector;
import com.codingchili.realm.instance.model.events.ListEntityLootEvent;
import com.codingchili.realm.instance.model.items.Item;

import java.util.*;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * A lootable entity, when a creature peeks inside the container
 * the contents are shown through an event. The creature is then
 * subscribed to changes to the containers contents until the
 * container expires, is emptied or the creature leaves the instance.
 */
public class LootableEntity extends SimpleEntity {
    private static final int LOOT_DECAY_TIME = GameContext.secondsToTicks(600);
    private Set<String> subscribers = new HashSet<>();
    private List<Item> items;


    public LootableEntity(Vector vector, List<Item> items) {
        this.vector = vector;
        this.items = items;
    }

    @Override
    public void setContext(GameContext game) {
        super.setContext(game);

        game.ticker(ticker -> {
            game.remove(this);
            ticker.disable();
        }, LOOT_DECAY_TIME);
    }

    public void subscribe(Creature source) {
        source.handle(createEvent());
        subscribers.add(source.getId());
    }

    public Item takeItem(String itemId) {
        for (Item item : items) {
            if (item.getId().equals(itemId)) {

                if (items.size() == 0) {
                    game.remove(this);
                }

                items.remove(item);
                notifySubscribers();

                return item;
            }
        }
        throw new CoreRuntimeException("Item is not available.");
    }

    private void notifySubscribers() {
        ListEntityLootEvent event = createEvent();

        subscribers.removeIf(subscriber -> {
            if (game.creatures().exists(subscriber)) {
                game.getById(subscriber).handle(event);
                return false;
            } else {
                // subscriber not available - unsubscribe.
                return true;
            }
        });
    }

    private ListEntityLootEvent createEvent() {
        ListEntityLootEvent event = new ListEntityLootEvent();
        event.setLootList(items);
        event.setTargetId(getId());
        return event;
    }

}

