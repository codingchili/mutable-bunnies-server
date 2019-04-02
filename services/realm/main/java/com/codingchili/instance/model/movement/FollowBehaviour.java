package com.codingchili.instance.model.movement;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.stats.Attribute;

/**
 * A behaviour that causes a source creature to follow a target creature.
 * The behaviour is in effect until either creature leaves the context.
 */
public class FollowBehaviour implements MovementBehaviour {
    private static float FOLLOW_RANGE = 128.0f;
    private Creature source;
    private Creature target;

    /**
     * @param source the creature that will be following the target.
     * @param target the creature being followed by source.
     */
    public FollowBehaviour(Creature source, Creature target) {
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

        if (vector.targetDistance(targetX, targetY) > FOLLOW_RANGE) {
            float direction = vector.targetAngle(targetX, targetY);
            vector.setDirection(direction);
            vector.setVelocity((float) source.getStats().get(Attribute.movement));
            game.movement().update(source);
        } else {
            if (vector.isMoving()) {
                vector.stop();
                game.movement().update(source);
            }
        }
    }
}
