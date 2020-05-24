package com.codingchili.instance.controller;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.admin.AdminEvent;
import com.codingchili.instance.model.dialog.AdminEngine;
import com.codingchili.instance.transport.InstanceRequest;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 *
 * Handles admin functions.
 */
public class AdminHandler implements GameHandler {
    private AdminEngine engine;
    private GameContext game;

    public AdminHandler(GameContext game) {
        this.game = game;
        this.engine = new AdminEngine(game);
    }

    @Api
    public void admin(InstanceRequest request) {
        engine.handle(request.raw(AdminEvent.class));
    }
}
