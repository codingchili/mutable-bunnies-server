package com.codingchili.instance.model.spells;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.events.MovementEvent;
import com.codingchili.instance.model.stats.Attribute;

/**
 * @author Robin Duda
 * <p>
 * Handles movement in the game.
 */
public class MovementEngine {
    private GameContext game;

    public MovementEngine(GameContext game) {
        this.game = game;

        game.ticker(ticker -> {
            game.creatures().all().forEach(creature -> creature.getVector().forward());
        }, 1);
    }

    /**
     * Updates the vector of the given creature.
     *
     * @param vector     the updated vector - may only set direction and velocity.
     *                   velocity is constrained to 0 and the movement attribute of the creature.
     * @param creatureId the creature to update the vector of.
     */
    public void update(Vector vector, String creatureId) {
        Creature creature = game.getById(creatureId);

        Vector current = creature.getVector();
        current.setDirection(vector.getDirection());

        if (current.getVelocity() == 0) {
            current.setAcceleration(Vector.ACCELERATION_BASE);
        }

        // make sure the player cannot arbitrarily update movement.
        if (vector.getVelocity() == 0) {
            current.setVelocity(0);
        } else {
            current.setVelocity(Math.min(creature.getStats().get(Attribute.movement), vector.getVelocity()));
        }
        MovementEvent event = new MovementEvent(current, creatureId);
        game.publish(event);
    }
}
