package com.codingchili.instance.model.movement;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

/**
 * Plays an animation on the client side.
 */
public class AnimationEvent implements Event {
    private String targetId;
    private Animation animation;

    /**
     * @param target the entity to animate.
     * @param animation the animation to play.
     */
    public AnimationEvent(String target, Animation animation) {
        this.targetId = target;
        this.animation = animation;
    }

    public String getTargetId() {
        return targetId;
    }

    public AnimationEvent setTargetId(String targetId) {
        this.targetId = targetId;
        return this;
    }

    public Animation getAnimation() {
        return animation;
    }

    public AnimationEvent setAnimation(String Animation) {
        this.animation = animation;
        return this;
    }

    @Override
    public EventType getRoute() {
        return EventType.animation;
    }
}
