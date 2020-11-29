package com.codingchili.instance.controller;

import com.codingchili.instance.context.*;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.entity.Point;
import com.codingchili.instance.model.events.*;
import com.codingchili.instance.model.npc.SpawnEngine;
import com.codingchili.instance.transport.InstanceRequest;
import io.vertx.core.Future;

import java.util.HashSet;
import java.util.Set;

import com.codingchili.core.context.DeploymentAware;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.*;

import static com.codingchili.core.protocol.RoleMap.PUBLIC;

/**
 * @author Robin Duda
 * <p>
 * Handles players in an settings.
 */
@Roles(PUBLIC)
public class InstanceHandler implements CoreHandler, DeploymentAware {
    private final Protocol<Request> protocol = new Protocol<>(this);
    private Set<GameHandler> handlers = new HashSet<>();
    private InstanceContext context;
    private GameContext game;

    public InstanceHandler(InstanceContext context) {
        this.context = context;

        game = new GameContext(context);

        handlers.add(new MovementHandler(game));
        handlers.add(new TradeHandler(game));
        handlers.add(new SpellHandler(game));
        handlers.add(new DialogHandler(game));
        handlers.add(new InventoryHandler(game));
        handlers.add(new QuestHandler(game));
        handlers.add(new AdminHandler(game));
        handlers.add(new DesignHandler(game));
        handlers.add(new SkillHandler(game));

        handlers.forEach(protocol::annotated);
    }

    @Override
    public void start(Future<Void> future) {
        game.queue(() -> {
            InstanceSettings settings = context.settings();
            SpawnEngine spawner = game.spawner();

            settings.getStructures().forEach(entity -> {
                Point point = entity.getPoint();
                spawner.structure(entity.getId(), point.getX(), point.getY());
            });

            settings.getNpcs().forEach(npc -> {
                Point point = npc.getPoint();
                spawner.npc(npc.getId(), point.getX(), point.getY());
            });
            future.complete();
        });
    }

    @Api
    public void ping(Request request) {
        request.accept();
    }

    @Api
    public void join(InstanceRequest request) {
        JoinMessage join = request.raw(JoinMessage.class);
        PlayerCreature player = join.getPlayer();

        context.onPlayerJoin(join);
        game.add(player);

        request.write(new ConnectEvent(game, player));
    }

    @Api
    public void leave(InstanceRequest request) {
        LeaveMessage leave = request.raw(LeaveMessage.class);

        if (game.exists(leave.getPlayerName())) {
            PlayerCreature player = game.getById(leave.getPlayerName());
            player.getVector().stop();

            handlers.forEach(h -> h.onPlayerLeave(player.getId()));

            game.remove(player);
            game.instance().save(player);
            context.onPlayerLeave(leave);
        }
    }

    @Override
    public void handle(Request request) {
        protocol.process(request);
    }

    @Override
    public String address() {
        return context.address();
    }

    @Override
    public void stop(Future<Void> future) {
        game.close();
        context.onInstanceStopped(future, context.realm().getId(), context.settings().getId());
    }

    @Override
    public int instances() {
        return 1;
    }
}
