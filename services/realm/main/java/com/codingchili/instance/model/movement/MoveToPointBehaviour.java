package com.codingchili.instance.model.movement;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.events.MovementEvent;
import com.codingchili.instance.model.stats.Attribute;

/**
 * A behaviour that moves the given target creature towards the given point.
 * The behaviour is active until the player leaves the context or the target point reached.
 */
public class MoveToPointBehaviour implements MovementBehaviour {
    private long ttl;
    private Creature source;
    private float targetX;
    private float targetY;

    /**
     * @param source  the creature that will move to the point.
     * @param targetX the target x coordinate.
     * @param targetY the target y coordinate.
     */
    public MoveToPointBehaviour(Creature source, float targetX, float targetY) {
        this.source = source;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public MovementBehaviour activate(GameContext game) {
        Vector vector = source.getVector();
        float direction = vector.targetAngle(targetX, targetY);
        vector.setDirection(direction);
        vector.setVelocity((float) source.getStats().get(Attribute.movement));

        this.ttl = (int) ((vector.targetDistance(targetX, targetY)
            / vector.getVelocity()) * 1000)
            + System.currentTimeMillis();

        game.publish(new MovementEvent(vector, source.getId()));
        return this;
    }

    @Override
    public void update(GameContext game) {
        // no action.
    }

    @Override
    public boolean active(GameContext game) {
        boolean active =  ttl > System.currentTimeMillis() && game.exists(source.getId());
        if (!active) {
            source.getVector().setVelocity(0);
            game.movement().update(source.getVector(), source.getId());
        }
        return active;
    }
}
