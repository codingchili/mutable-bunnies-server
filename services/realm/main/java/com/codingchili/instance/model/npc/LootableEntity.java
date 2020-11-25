package com.codingchili.instance.model.npc;

import com.codingchili.core.files.Configurations;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.items.ListEntityLootEvent;
import com.codingchili.instance.model.items.Item;

import java.util.*;

import com.codingchili.core.context.CoreRuntimeException;

import static com.codingchili.instance.model.entity.Interaction.DESCRIPTION;

/**
 * @author Robin Duda
 * <p>
 * A lootable entity, when a creature peeks inside the container
 * the contents are shown through an event. The creature is then
 * subscribed to changes to the containers contents until the
 * container expires, is emptied or the creature leaves the instance.
 */
public class LootableEntity extends SimpleEntity {
    private static transient final Random random = new Random();
    private final Set<String> subscribers = new HashSet<>();
    private final List<Item> items;
    private String corpseOf;
    private long timer;

    /**
     * Creates a new container used to store the loot of the given corpse.
     *
     * @param source the entity that is spawning the loot.
     * @param items  the items to be contained in the container.
     * @return an entity that can be added to the game context.
     */
    public static LootableEntity fromCorpse(Entity source, List<Item> items) {
        Vector vector = source.getVector().copy()
                .setY(source.getVector().getY() + configuration().getCorpseOffsetY());

        LootableEntity entity = new LootableEntity(vector, items)
                .setCorpseOf(source.getId());

        Model model = configuration().getGravestone();
        model.setRevertX(random.nextBoolean());
        entity.setModel(model);

        entity.setName(configuration().getName());
        entity.getAttributes().put(DESCRIPTION,
                String.format(configuration().getDescription(), source.getName())
        );
        return entity;
    }

    /**
     * Creates a new loot container with a single item.
     *
     * @param source the location at which to drop loot.
     * @param item   the items to be contained in the container.
     * @return an entity that can be added to the game context.
     */
    public static LootableEntity dropped(Vector source, Item item) {
        Vector vector = source.copy();

        float distance = configuration().getDropDistance();
        float offset = random.nextInt(360);
        vector.setY(vector.getY() + (float) Math.cos(Math.toRadians(offset)) * distance);
        vector.setX(vector.getX() + (float) Math.sin(Math.toRadians(offset)) * distance);

        List<Item> items = new ArrayList<>();
        items.add(item);

        Model model = configuration().getItem();
        model.setGraphics(item.getIcon());

        LootableEntity entity = new LootableEntity(vector, items);
        entity.setName(item.getName());
        entity.setModel(model);

        return entity;
    }

    private LootableEntity(Vector vector, List<Item> items) {
        this.vector = vector.copy();
        this.items = items;
        this.interactions.add(Interaction.LOOT);
        this.vector.stop();
    }

    private static LootableConfiguration configuration() {
        return Configurations.get(LootableConfiguration.PATH, LootableConfiguration.class);
    }

    public String getCorpseOf() {
        return corpseOf;
    }

    public LootableEntity setCorpseOf(String corpseOf) {
        this.corpseOf = corpseOf;
        return this;
    }

    public boolean isCorpse() {
        return corpseOf != null;
    }

    @Override
    public void setContext(GameContext game) {
        super.setContext(game);
        long decay = configuration().getDecay();
        timer = game.instance().timer(decay, (id) -> {
            game.remove(this);
        });
    }

    @Override
    public void removed() {
        game.instance().cancel(timer);
    }

    public void subscribe(Creature source) {
        source.handle(createEvent());
        subscribers.add(source.getId());
    }

    public List<Item> getItems() {
        return items;
    }

    public void unsubscribe(String targetId) {
        subscribers.remove(targetId);
    }

    private boolean removeWhenEmpty() {
        return configuration().isRemoveWhenEmpty();
    }

    public Item takeItem(String itemId) {
        Item found = null;

        for (Item item : items) {
            if (item.getId().equals(itemId)) {
                found = item;
                break;
            }
        }
        if (found != null) {
            items.remove(found);
            notifySubscribers();

            if (!isCorpse() && items.size() == 0) {
                game.remove(this);
            }
            return found;
        } else {
            throw new CoreRuntimeException("Item is not available.");
        }
    }

    public boolean subscribed(Creature source) {
        return subscribers.contains(source.getId());
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

