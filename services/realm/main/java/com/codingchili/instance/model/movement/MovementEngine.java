package com.codingchili.instance.model.movement;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.events.*;
import com.codingchili.instance.model.stats.Attribute;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Handles movement in the game.
 */
public class MovementEngine {
    private static final float BEHAVIOUR_UPDATE_INTERVAL = 0.5f;
    private Map<Creature, MovementBehaviour> behaviours = new HashMap<>();
    private GameContext game;

    /**
     * The movement engine handles player and npc movement.
     *
     * @param game the game context the engine manages.
     */
    public MovementEngine(GameContext game) {
        this.game = game;

        game.ticker(ticker -> {
            behaviours.entrySet().removeIf(entry -> {
                entry.getValue().update(game);
                return !entry.getValue().active(game);
            });
        }, GameContext.secondsToTicks(BEHAVIOUR_UPDATE_INTERVAL));

        game.ticker(ticker -> {
            float delta = ticker.delta();
            game.creatures().all().forEach(creature -> {
                creature.getVector().forward(delta);
            });
        }, GameContext.onAllTicks());
    }

    /**
     * Updates the vector of the given creature.
     *
     * @param creature the creature with a modified vector to be updated.
     */
    public void update(Creature creature) {
        Vector current = creature.getVector();

        if (current.getVelocity() == 0) {
            current.setAcceleration(Vector.ACCELERATION_BASE);
        }

        // make sure the player cannot arbitrarily update movement.
        if (current.getVelocity() == 0) {
            current.setVelocity(0);
        } else {
            current.setVelocity((float) Math.min(creature.getStats().get(Attribute.movement), current.getVelocity()));
        }
        MovementEvent event = new MovementEvent(current, creature.getId());
        game.publish(event);
    }

    /**
     * Updates the vector and cancels any existing behaviors.
     *
     * @param creature the creature to set the vector for.
     */
    public void set(Creature creature) {
        cancel(creature);
        update(creature);
    }

    /**
     * Stops following an existing target.
     *
     * @param creature the creature that will stop following its target.
     */
    public void unfollow(Creature creature) {
        Vector vector = creature.getVector();

        vector.setVelocity(0);
        update(creature);
        behaviours.remove(creature);
    }

    /**
     * Instructs a creature to follow the given target.
     *
     * @param source the creature that is following.
     * @param target the creature that is to be followed.
     */
    public void follow(Creature source, Creature target) {
        behaviours.put(source, new FollowBehaviour(source, target));
    }

    /**
     * Makes the source move in the opposite of the target.
     *
     * @param source the source to be moved.
     * @param target the target to move the source relative to.
     */
    public void flee(Creature source, Creature target) {
        behaviours.put(source, new FleeBehaviour(source, target));
    }

    /**
     * Moves to a location specified by the given coordinates.
     *
     * @param creature the creature that is moving.
     * @param x        the x position to move to.
     * @param y        the y position to move to.
     */
    public void moveTo(Creature creature, float x, float y) {
        behaviours.put(creature,
                new MoveToPointBehaviour(creature, x, y)
                        .activate(game));
    }


    /**
     * Cancels the behavior set on the given creature.
     *
     * @param creature the creature to cancel behaviours on.
     */
    public void cancel(Creature creature) {
        behaviours.remove(creature);
    }

    /**
     * Attempts to transfer the given player creature to the given instance.
     * Has no effect if the player is already in the target instance.
     *
     * @param creature the creature which will be travelling.
     * @param instance the name of the instance being travelled to.
     */
    public void travel(PlayerCreature creature, String instance) {
        if (!creature.getInstance().equals(instance)) {
            creature.setFromAnotherInstance(true);

            game.instance().sendRealm(new PlayerTravelMessage(creature, instance)).setHandler(done -> {
                if (done.succeeded()) {
                    JsonObject response = Serializer.json(done.result());
                    ResponseStatus status = ResponseStatus.valueOf(response.getString(PROTOCOL_STATUS));

                    if (status.equals(ResponseStatus.ACCEPTED)) {
                        game.remove(creature);
                    } else {
                        creature.handle(new ErrorEvent(response.getString(PROTOCOL_MESSAGE)));
                        creature.setFromAnotherInstance(false);
                    }
                } else {
                    creature.handle(new ErrorEvent(done.cause().getMessage()));
                    creature.setFromAnotherInstance(false);
                }
            });
        }
    }
}
