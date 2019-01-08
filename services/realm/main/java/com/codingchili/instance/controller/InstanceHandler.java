package com.codingchili.instance.controller;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.InstanceContext;
import com.codingchili.instance.model.entity.Entity;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.events.*;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.transport.InstanceRequest;
import io.vertx.core.Future;

import java.util.*;

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

        handlers.forEach(protocol::annotated);
    }

    @Api
    public void ping(Request request) {
        request.accept();
    }

    @Api
    public void join(InstanceRequest request) {
        JoinMessage join = request.raw(JoinMessage.class);
        PlayerCreature creature = join.getPlayer();
        List<Entity> entities = new ArrayList<>();

        entities.add(creature);
        entities.addAll(game.entities().all());
        entities.addAll(game.creatures().all());

        context.onPlayerJoin(join);
        game.add(creature);

        request.write(new ConnectEvent(creature, entities));
    }

    @Api
    public void leave(InstanceRequest request) {
        LeaveMessage leave = request.raw(LeaveMessage.class);
        PlayerCreature player = game.getById(leave.getPlayerName());
        player.getVector().setVelocity(0);

        handlers.forEach(h -> h.onPlayerLeave(player.getId()));

        game.remove(player);
        game.instance().sendRealm(new SavePlayerMessage(player));
        context.onPlayerLeave(leave);
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
        context.onInstanceStopped(future, context.realm().getNode(), context.settings().getName());
    }

    @Override
    public void start(Future<Void> future) {
        //context.onInstanceStarted(context.realm().getName(), context.settings().getName());
        future.complete();
    }

    // todo: only log listener started if handler does not implement start or ignore it altogether?
    // todo: instance metadata is wrong in the listener.start logging, fix.
    // todo: log loaded X instances, X realms, X classes, X afflictions, X spells - once in the static loader.

    @Override
    public int instances() {
        return 1;
    }
}
