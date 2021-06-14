package com.codingchili.instance.model.npc;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.items.Item;
import com.codingchili.instance.model.items.ListEntityLootEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Serializer;

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
    private transient Entity corpse;
    private final List<Item> items;
    private Vector sourceVector;
    private String corpseOf;
    private long timer;

    private LootableEntity(Vector vector, List<Item> items) {
        this.vector = vector.copy();
        this.items = items;
        this.interactions.add(Interaction.LOOT);
        this.vector.stop();
    }

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
                .setCorpse(source);

        Model model = configuration().getGravestone()
                .setRevertX(random.nextBoolean());

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
        // space out dropped items but avoid dropping behind an entity.
        float offset = random.nextInt(240) + 240;
        float distance = configuration().getDropDistance();

        Vector target = source.copy()
                .setY(source.getY() + (float) Math.cos(Math.toRadians(offset)) * distance)
                .setX(source.getX() + (float) Math.sin(Math.toRadians(offset)) * distance);

        Model model = configuration().getItem()
                .setGraphics(item.getIcon());

        List<Item> items = new ArrayList<>();
        items.add(item);

        LootableEntity entity = new LootableEntity(target, items)
                .setSourceVector(source.copy());

        entity.setModel(model);
        entity.setName(item.getName());

        return entity;
    }

    private LootableEntity setSourceVector(Vector source) {
        this.sourceVector = source;
        return this;
    }

    @JsonProperty
    public Vector getSourceVector() {
        return sourceVector;
    }

    private static LootableConfiguration configuration() {
        return Configurations.get(LootableConfiguration.PATH, LootableConfiguration.class);
    }

    public LootableEntity setCorpse(Entity corpse) {
        this.corpse = corpse;
        this.corpseOf = corpse.getId();
        return this;
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

        if (isCorpse()) {
            setTintFromPlayerClass();
        }
        timer = game.instance().timer(decay, (id) -> game.remove(this));
    }

    private void setTintFromPlayerClass() {
        if (corpse instanceof PlayerCreature) {
            game.classes().getById(((PlayerCreature) corpse).getClassId()).ifPresent(theClass ->
                    model.setTint(theClass.getTheme()));
        }
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

    private void changed() {
        notifySubscribers();

        if (!isCorpse() && items.size() == 0) {
            game.remove(this);
        }
    }

    public Collection<Item> takeAll() {
        var items = new ArrayList<>(this.items);
        this.items.clear();
        changed();
        return items;
    }

    public Item takeItem(String itemId) {
        var found = items.stream()
                .filter(entry -> entry.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CoreRuntimeException("Item is not available."));

        items.remove(found);
        changed();
        return found;
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

