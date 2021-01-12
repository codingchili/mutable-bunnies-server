package com.codingchili.instance.controller;

import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Api;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.admin.AuthorizationException;
import com.codingchili.instance.model.designer.DesignerRequest;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.npc.SpawnEngine;
import com.codingchili.instance.transport.InstanceRequest;

import java.util.function.Consumer;

/**
 * apply permissions
 * filter by inventory, no filter for admin :))
 * use inventory "blueprints" or "sandbags "
 * add to configuration + persist async
 */
public class DesignHandler implements GameHandler {
    private GameContext game;
    private SpawnEngine spawner;

    public DesignHandler(GameContext game) {
        this.game = game;
        spawner = game.spawner();
    }

    @Api
    public void modify(InstanceRequest request) {
        authorize(request, () -> {
            DesignerRequest designer = request.raw(DesignerRequest.class);
            switch (designer.getType()) {
                case SPAWN:
                    spawner.add(designer);
                    break;
                case DESPAWN:
                    spawner.remove(designer);
                    break;
            }
        });
    }

    @Api
    public void npc_registry(InstanceRequest request) {
        authorize(request, () -> request.write(spawner.npcs().toBuffer()));
    }

    @Api
    public void structure_registry(InstanceRequest request) {
        authorize(request, () -> request.write(spawner.entities().toBuffer()));
    }

    private void authorize(InstanceRequest request, Runnable authorized) {
        if (game.instance().realm().isAdmin(request.account())) {
            authorized.run();
        } else {
            request.error(new AuthorizationException());
        }
    }
}
