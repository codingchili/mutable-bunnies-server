package com.codingchili.instance.model.movement;

import java.util.HashMap;
import java.util.Map;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.events.ErrorEvent;
import com.codingchili.instance.model.events.MovementEvent;
import com.codingchili.instance.model.events.PlayerTravelMessage;
import com.codingchili.instance.model.stats.Attribute;
import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_MESSAGE;
import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_STATUS;

import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 * <p>
 * Handles movement in the game.
 */
public class MovementEngine {
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
        }, GameContext.secondsToTicks(0.5));

        game.ticker(ticker -> {
            float delta = ticker.delta();
            game.creatures().all().forEach(creature -> {
                creature.getVector().forward(delta);
            });
        }, 1);
    }

    /**
     * Updates the vector of the given creature.
     *
     * @param vector     the updated vector - may only set direction and velocity.
     *                   velocity is constrained between 0 and the movement attribute of the creature.
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
     * Updates the vector and cancels any existing behaviors.
     * @param vector the updated vector.
     * @param creatureId the creature id.
     */
    public void set(Vector vector, String creatureId) {
        cancel(game.getById(creatureId));
        update(vector, creatureId);
    }

    /**
     * Stops following an existing target.
     *
     * @param creature the creature that will stop following its target.
     */
    public void unfollow(Creature creature) {
        Vector vector = creature.getVector();

        vector.setVelocity(0);
        update(vector, creature.getId());
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
