package com.codingchili.instance.model.movement;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.stats.Attribute;

/**
 * A behaviour that causes one creature to flee from another.
 */
public class FleeBehaviour implements MovementBehaviour {
    private static float FLEE_RANGE = 512.0f;
    private Creature source;
    private Creature target;

    /**
     * @param source the creature that will be fleeing the target.
     * @param target the creature being fled from by source.
     */
    public FleeBehaviour(Creature source, Creature target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public boolean active(GameContext game) {
        return game.exists(source.getId()) && game.exists(target.getId());
    }

    @Override
    public void update(GameContext game) {
        Vector vector = source.getVector();
        Vector following = target.getVector();

        float targetX = following.getX();
        float targetY = following.getY();

        if (vector.targetDistance(targetX, targetY) < FLEE_RANGE) {
            float direction = vector.targetAngle(targetX, targetY);
            direction += Math.toRadians(180);
            vector.setDirection(direction);
            vector.setVelocity((float) source.getStats().get(Attribute.movement));
            game.movement().update(source);
        } else {
            if (vector.isMoving()) {
                vector.stop();
                game.publish(new MovementEvent(vector, source.getId()));
            }
        }
    }
}
