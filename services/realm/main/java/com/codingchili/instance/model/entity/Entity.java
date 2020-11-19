package com.codingchili.instance.model.entity;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.events.Event;

import java.util.*;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * An entity that exists in the game, can be a player, a house or anything else.
 */
public interface Entity extends Storable, Receiver<Event> {

    /**
     * @return the display name of the entity.
     */
    String getName();

    /**
     * @return the unique identifier of the entity.
     */
    String getId();

    /**
     * @return the position of the entity in the game world.
     */
    Vector getVector();

    /**
     * @return a graphical representation of the entity.
     */
    Model getModel();

    /**
     * @return a set of names of the events that this entity is a subscriber of.
     */
    Set<String> getInteractions();

    /**
     * @return contains events the entity subscribes to.
     */
    EventProtocol protocol();

    /**
     * @return an attribute map that can be used by scripts.
     */
    Map<String, Object> getAttributes();


    /**
     * @return true if the entity is a creature.
     */
    boolean isCreature();

    /**
     * Called before adding the creature to the instance.
     *
     * @param game the game context.
     */
    void setContext(GameContext game);

    /**
     * Callback for when the entity is completely added to the instance.
     */
    default void joined() {};

    /**
     * Callback for when the entity is completely removed from the instance.
     * Any tickers started on game context needs to be disabled here.
     */
    default void removed() {};
}
