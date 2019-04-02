package com.codingchili.instance.model.movement;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Vector;

/**
 * A behaviour that moves the given target creature towards the given point.
 * The behaviour is active until the player leaves the context or the target point reached.
 */
public class MoveToPointBehaviour implements MovementBehaviour {
    private Creature source;
    private Vector target;
    private boolean onTheLeft;
    private boolean onTheTop;

    /**
     * @param source the creature that will move to the point.
     * @param target the target vector.
     */
    public MoveToPointBehaviour(Creature source, Vector target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public MovementBehaviour activate(GameContext game) {
        Vector vector = source.getVector();
        float direction = vector.targetAngle(target.getX(), target.getY());
        vector.setDirection(direction);
        vector.setVelocity(target.getVelocity());

        onTheLeft = vector.toLeftOf(target);
        onTheTop = vector.onTopOf(target);

        game.movement().update(source);
        return this;
    }

    @Override
    public void update(GameContext game) {
        // no action.
    }

    @Override
    public boolean active(GameContext game) {
        Vector vector = source.getVector();

        // check if left/right and bottom/top relations have changed.
        boolean active = (!(onTheLeft ^ vector.toLeftOf(target) &&
                (onTheTop ^ vector.onTopOf(target)))) &&
                game.exists(source.getId());

        if (!active) {
            source.getVector().stop();
            game.movement().update(source);
        }
        return active;
    }
}
