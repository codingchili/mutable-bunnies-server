package com.codingchili.instance.controller;

import com.codingchili.core.protocol.Api;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.designer.DesignerRequest;
import com.codingchili.instance.model.entity.EntityDB;
import com.codingchili.instance.model.npc.SpawnEngine;
import com.codingchili.instance.transport.InstanceRequest;

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
        DesignerRequest designer = request.raw(DesignerRequest.class);
        switch (designer.getType()) {
            case SPAWN:
                spawner.add(designer);
                break;
            case DESPAWN:
                spawner.remove(designer);
                break;
        }
    }

    @Api
    public void registry(InstanceRequest request) {
        request.write(spawner.entities().toBuffer());
    }
}
