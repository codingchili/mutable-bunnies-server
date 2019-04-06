package com.codingchili.instance.controller;

import com.codingchili.core.protocol.Api;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.events.MovementEvent;
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
}
