package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.context.InstanceContext;
import com.codingchili.realm.instance.model.entity.Entity;
import com.codingchili.realm.instance.model.entity.PlayerCreature;
import com.codingchili.realm.instance.model.events.JoinMessage;
import com.codingchili.realm.instance.model.events.LeaveMessage;
import com.codingchili.realm.instance.transport.ControlRequest;
import io.vertx.core.Future;

import com.codingchili.core.context.DeploymentAware;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.*;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.RoleMap.PUBLIC;

/**
 * @author Robin Duda
 *
 * Handles players in an settings.
 */
@Roles(PUBLIC)
public class InstanceHandler implements CoreHandler, DeploymentAware {
    private final Protocol<Request> protocol = new Protocol<>(this);
    private InstanceContext context;
    private GameContext game;

    public InstanceHandler(InstanceContext context) {
        this.context = context;

        game = new GameContext(context);

        protocol.annotated(new MovementHandler(game));
        protocol.annotated(new TradeHandler(game));
        protocol.annotated(new SpellHandler(game));
        protocol.annotated(new DialogHandler(game));
    }

    @Api
    public void ping(Request request) {
        request.accept();
    }

    @Api(route = CLIENT_INSTANCE_JOIN)
    public void join(ControlRequest request) {
        JoinMessage join = request.raw(JoinMessage.class);
        PlayerCreature creature = join.getPlayer();
        game.add(creature);
        context.onPlayerJoin(join);
        request.accept();
    }

    @Api(route = CLIENT_INSTANCE_LEAVE)
    public void leave(ControlRequest request) {
        LeaveMessage leave = request.raw(LeaveMessage.class);
        Entity player = game.getById(leave.getPlayerName());
        context.onPlayerLeave(leave);
        game.remove(player);
        request.accept();
    }

    @Override
    public void handle(Request request) {
        protocol.get(request.route()).submit(request);
    }

    @Override
    public String address() {
        return context.address();
    }

    @Override
    public void stop(Future<Void> future) {
        context.onInstanceStopped(future, context.realm().getName(), context.settings().getName());
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
