package com.codingchili.instance.model.movement;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.events.ErrorEvent;
import com.codingchili.instance.model.events.PlayerTravelMessage;
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
    private static final float BEHAVIOUR_UPDATE_INTERVAL = 0.1f;
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
            for (Entity creature : game.creatures().all()) {
                creature.getVector().forward(ticker);
            }
        }, GameContext.onAllTicks());
    }

    /**
     * Updates the vector of the given creature.
     *
     * @param creature the creature with a modified vector to be updated.
     */
    public void update(Creature creature) {
        Vector vector = creature.getVector();

        // make sure the player cannot arbitrarily update movement.
        if (vector.isMoving()) {
            vector.setVelocity((float) Math.min(creature.getStats().get(Attribute.movement), vector.getVelocity()));
        }

        MovementEvent event = new MovementEvent(vector, creature.getId());
        game.publish(event);
    }

    /**
     * Updates the vector and cancels any existing behaviors.
     *
     * @param creature the creature to modify the vector of.
     * @param vector   contains new vector properties, velocity and direction.
     */
    public void move(Creature creature, Vector vector) {
        tryMove(creature, () -> {
            creature.getVector()
                    .setVelocity(vector.getVelocity())
                    .setDirection(vector.getDirection());

            cancel(creature);
            update(creature);
        });
    }

    private void tryMove(Creature creature, Runnable runnable) {
        if (game.spells().mobile(creature)) {
            runnable.run();
        }
    }

    /**
     * Stops following an existing target.
     *
     * @param creature the creature that will stop following its target.
     */
    public void unfollow(Creature creature) {
        Vector vector = creature.getVector();

        vector.stop();
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
        tryActivate(source, new FollowBehaviour(source, target));
    }

    private void tryActivate(Creature creature, MovementBehaviour behavior) {
        // allow blocking behaviours?
        tryMove(creature, () -> {
            behaviours.put(creature, behavior);
            behavior.activate(game);
        });
    }

    /**
     * Stops moving the given source and cancels any behaviours.
     *
     * @param source the creature to stop.
     */
    public void stop(Creature source) {
        if (source.getVector().isMoving() || behaviours.containsKey(source)) {
            source.getVector().stop();
            cancel(source);
            update(source);
        }
    }

    /**
     * Makes the source move in the opposite of the target.
     *
     * @param source the source to be moved.
     * @param target the target to move the source relative to.
     */
    public void flee(Creature source, Creature target) {
        tryActivate(source, new FleeBehaviour(source, target));
    }

    /**
     * Moves to a location specified by the given coordinates.
     *
     * @param creature the creature that is moving.
     * @param vector   the target vector.
     */
    public void moveTo(Creature creature, Vector vector) {
        tryActivate(creature,
                new MoveToPointBehaviour(creature, vector));
    }


    /**
     * Cancels the behavior set on the given creature and stops moving it.
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
                        game.spells().cancel(creature);
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
