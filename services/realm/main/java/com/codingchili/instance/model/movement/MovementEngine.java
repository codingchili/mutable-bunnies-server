package com.codingchili.instance.model.movement;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.events.MovementEvent;
import com.codingchili.instance.model.stats.Attribute;

/**
 * @author Robin Duda
 * <p>
 * Handles movement in the game.
 */
public class MovementEngine {
    private static final int FOLLOW_RANGE = 128;
    private GameContext game;

    public MovementEngine(GameContext game) {
        this.game = game;

        game.ticker(ticker -> {
            game.creatures().all().forEach(creature -> {
                Vector vector = creature.getVector();

                if (vector.isFollowing()) {
                    Vector following = vector.getFollowing();
                    vector.setTarget(following.getX(), following.getY());
                    vector.setVelocity(1.0f);
                    game.publish(new MovementEvent(vector, creature.getId()));
                    //}
                } else if (vector.hasTarget()) {
                    if (!targetOutOfRange(vector, vector.getTargetX(), vector.getTargetY())) {
                        vector.clearTarget();
                        vector.setVelocity(0);
                        game.publish(new MovementEvent(vector, creature.getId()));
                    }
                }
            });
        }, GameContext.secondsToTicks(0.5));

        game.ticker(ticker -> {
            float delta = ticker.delta();
            game.creatures().all().forEach(creature -> {
                creature.getVector().forward(delta);
            });
        }, 1);
    }

    private boolean targetOutOfRange(Vector vector, float targetX, float targetY) {
        float distanceX = Math.abs(vector.getX() - targetX);
        float distanceY = Math.abs(vector.getY() - targetY);
        return (distanceX > FOLLOW_RANGE || distanceY > FOLLOW_RANGE);
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
            current.setVelocity((float) Math.min(creature.getStats().get(Attribute.movement), vector.getVelocity()));
        }
        MovementEvent event = new MovementEvent(current, creatureId);
        game.publish(event);
    }

    /**
     * Stops following an existing target.
     *
     * @param creature the creature that will stop following its target.
     */
    public void unfollow(Creature creature) {
        Vector vector = creature.getVector();

        vector.setFollowing(null);
        vector.setVelocity(0);

        game.publish(new MovementEvent(vector, creature.getId()));
    }

    /**
     * Instructs a creature to follow the given target.
     *
     * @param source the creature that is following.
     * @param target the creature that is to be followed.
     */
    public void follow(Entity source, Entity target) {
        // update event is set in the vector update method.
        source.getVector().setFollowing(target.getVector());
    }

    /**
     * Moves to a location specified by the given coordinates.
     *
     * @param creature the creature that is moving.
     * @param x        the x position to move to.
     * @param y        the y position to move to.
     */
    public void moveTo(Creature creature, float x, float y) {
        creature.getVector()
                .setTarget(x, y)
                .setVelocity((float) creature.getStats().get(Attribute.movement));

        game.publish(new MovementEvent(creature.getVector(), creature.getId()));
    }
}
