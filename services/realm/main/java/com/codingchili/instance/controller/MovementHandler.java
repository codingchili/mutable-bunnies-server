package com.codingchili.instance.controller;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.events.MovementEvent;
import com.codingchili.instance.model.spells.MovementEngine;
import com.codingchili.instance.transport.InstanceRequest;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 *
 * Handles movement within the game world.
 */
public class MovementHandler implements GameHandler {
    private MovementEngine engine;

    /**
     * @param game creates a new movement handler for the given instance.
     */
    public MovementHandler(GameContext game) {
        this.engine = game.movement();
    }

    @Api
    public void move(InstanceRequest request) {
        MovementEvent movement = request.raw(MovementEvent.class);
        engine.update(movement.getVector(), request.target());
    }
}
