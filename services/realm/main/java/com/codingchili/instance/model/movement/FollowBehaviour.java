package com.codingchili.instance.model.movement;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.stats.Attribute;

import java.util.Random;

/**
 * A behaviour that causes a source creature to follow a target creature.
 * The behaviour is in effect until either creature leaves the context.
 */
public class FollowBehaviour implements MovementBehaviour {
    private static final Random random = new Random();
    private static int FOLLOW_RANGE = 96;
    private static int FOLLOW_OFFSET = 1; // offset needs to be less than the follow range.
    private static float DIRECTION_OFFSET = 0.5f; // offset needs to be less than the follow range.
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

        float targetX = following.getX() + random.nextInt(FOLLOW_OFFSET * 2) - FOLLOW_OFFSET;
        float targetY = following.getY() + random.nextInt(FOLLOW_OFFSET * 2) - FOLLOW_OFFSET;

        if (vector.distance(targetX, targetY) > FOLLOW_RANGE) {
            float direction = vector.targetAngle(targetX, targetY);
            vector.setDirection(direction + (random.nextFloat() * DIRECTION_OFFSET * 2) - DIRECTION_OFFSET);
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
