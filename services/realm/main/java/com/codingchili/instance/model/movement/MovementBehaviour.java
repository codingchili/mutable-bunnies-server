package com.codingchili.instance.model.movement;

import com.codingchili.instance.context.GameContext;

/**
 * A movement behaviour may be applied to 1..n creatures and is active
 * for a finite amount of time. Only one behaviour may be used at a time.
 * <p>
 * A behaviour may be cancelled by a player or a script, can be used
 * to move a player to a desired location (point and click) or have
 * players follow a creature or the reverse. It can even be used to
 * have NPC's flee from eachother or a player character.
 */
public interface MovementBehaviour {

    /**
     * @param game the game context the check is being run on.
     * @return true if the behaviour is still active.
     */
    boolean active(GameContext game);

    /**
     * Performs an update on the behaviour, this will not be called
     * for each tick but rather a few times per second.
     *
     * @param game the game context.
     */
    void update(GameContext game);

    /**
     * Called once when the behaviour is applied.
     *
     * @param game the game context.
     * @return fluent.
     */
    default MovementBehaviour activate(GameContext game) {
        return this;
    }
}
