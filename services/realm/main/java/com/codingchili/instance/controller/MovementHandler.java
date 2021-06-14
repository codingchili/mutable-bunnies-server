package com.codingchili.instance.controller;

import com.codingchili.core.protocol.Api;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.movement.AnimationEvent;
import com.codingchili.instance.model.movement.Animation;
import com.codingchili.instance.model.movement.MovementEvent;
import com.codingchili.instance.model.movement.MovementEngine;
import com.codingchili.instance.transport.InstanceRequest;

/**
 * @author Robin Duda
 * <p>
 * Handles movement within the game world.
 */
public class MovementHandler implements GameHandler {
    private MovementEngine engine;
    private GameContext game;

    /**
     * @param game creates a new movement handler for the given instance.
     */
    public MovementHandler(GameContext game) {
        this.engine = game.movement();
        this.game = game;
    }

    @Api
    public void dance_1(InstanceRequest request) {
        game.publish(new AnimationEvent(request.target(), Animation.dance_1));
    }

    @Api
    public void dance_2(InstanceRequest request) {
        game.publish(new AnimationEvent(request.target(), Animation.dance_2));
    }

    @Api
    public void jump(InstanceRequest request) {
        game.publish(new AnimationEvent(request.target(), Animation.jump));
    }

    @Api
    public void move(InstanceRequest request) {
        MovementEvent movement = request.raw(MovementEvent.class);
        engine.move(game.getById(request.target()), movement.getVector());
    }

    @Api
    public void moveTo(InstanceRequest request) {
        MovementEvent movement = request.raw(MovementEvent.class);
        Creature creature = game.getById(request.target());
        engine.moveTo(creature, movement.getVector());
    }

    @Api
    public void follow(InstanceRequest request) {
        MovementEvent movement = request.raw(MovementEvent.class);
        Creature source = game.getById(request.target());
        Creature target = game.getById(movement.getCreatureId());
        engine.follow(source, target);
    }
}
