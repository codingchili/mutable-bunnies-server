package com.codingchili.realm.controller;

import com.codingchili.instance.context.InstanceContext;
import com.codingchili.instance.context.InstanceSettings;
import com.codingchili.instance.controller.InstanceHandler;
import com.codingchili.instance.model.events.*;
import com.codingchili.instance.transport.InstanceRequest;
import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.model.RealmUpdate;
import com.codingchili.realm.model.UpdateResponse;
import io.vertx.core.*;

import java.util.*;

import com.codingchili.core.context.*;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.transport.Connection;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.*;

import static com.codingchili.common.Strings.NODE_AUTHENTICATION_REALMS;
import static com.codingchili.core.configuration.CoreStrings.ANY;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;
import static com.codingchili.core.protocol.RoleMap.PUBLIC;

/**
 * @author Robin Duda
 * <p>
 * Handles messaging between the realm and connected instances.
 */
@Roles(PUBLIC)
public class RealmInstanceHandler implements CoreHandler {
    private Protocol<Request> protocol = new Protocol<>(this);
    private RealmContext context;
    private Logger logger;
    private boolean registered = false;

    public RealmInstanceHandler(RealmContext context) {
        this.context = context;
        this.logger = context.logger(getClass());
        context.periodic(TimerSource.of(context::updateRate).setName(getClass().getSimpleName()), this::registerRealm);
        this.registerRealm(-1L);
    }

    @Override
    public void start(Promise<Void> future) {
        context.onRealmStarted(context.realm().getId());
        deployInstances(future);
    }

    @Api(route = ANY)
    public void any(InstanceRequest request) {
        Connection connection = context.connections().get(request.target());
        if (connection != null) {
            try {
                connection.write(request.data());
                request.accept();
            } catch (Exception e) {
                context.connections().remove(request.target());
                logger.onError(e);
                request.error(e);
            }
        } else {
            request.error(new CoreRuntimeException("Connection with id '" + request.target() + "' not available."));
        }
    }

    @Api
    public void travel(InstanceRequest request) {
        PlayerTravelMessage message = request.raw(PlayerTravelMessage.class);

        Optional<InstanceSettings> settings = context.instances().stream()
                .filter(instance -> instance.getId().equals(message.getInstance()))
                .findFirst();

        if (settings.isPresent()) {
            Connection connection = context.connections().get(request.target());

            if (connection != null) {
                context.onPlayerJoin(message.getPlayer());
                message.getPlayer().setInstance(message.getInstance());

                context.characters().update(message.getPlayer());
                String destination = context.setInstance(message.getPlayer(), connection);

                JoinMessage join = new JoinMessage()
                        .setPlayer(message.getPlayer())
                        .setRealmName(context.realm().getId());

                context.sendInstance(destination, join).onComplete(done -> {
                    if (done.succeeded()) {
                        connection.write(done.result());
                        request.accept();
                    } else {
                        request.error(new CoreRuntimeException("Failed to join instance " + destination));
                    }
                });
            }
        } else {
            request.error(new CoreRuntimeException(String.format(
                    "No such instance '%s'.", message.getInstance())));
        }
    }

    @Api
    public void save(InstanceRequest request) {
        SavePlayerMessage message = request.raw(SavePlayerMessage.class);
        context.characters().update(message.getCreature()).onComplete(request::result);
    }

    private void deployInstances(Promise<Void> future) {
        List<Future> futures = new ArrayList<>();
        for (InstanceSettings instance : context.instances()) {
            Promise<Void> deploy = Promise.promise();
            futures.add(deploy.future());

            context.blocking((blocking) -> {
                try {
                    InstanceContext instanceContext = new InstanceContext(context, instance);
                    InstanceHandler handler = new InstanceHandler(instanceContext);

                    // sometime in the future the instances will be deployed remotely - just deploy
                    // the instances on the same cluster.
                    instanceContext.listener(() -> new FasterBusListener().handler(handler)).onComplete((done) -> {
                        if (!done.succeeded()) {
                            context.onInstanceFailed(instance.getId(), done.cause());
                        }
                        blocking.complete();
                    });
                } catch (Exception e) {
                    blocking.fail(e);
                }
            }, FutureHelper.untyped(deploy));
        }
        CompositeFuture.all(futures).onComplete(done -> {
            if (done.succeeded()) {
                future.complete();
            } else {
                future.fail(done.cause());
            }
        });
    }


    private void registerRealm(Long handler) {
        RealmUpdate realm = new RealmUpdate(context.realm())
                .setPlayers(context.connections().size());

        context.bus().request(NODE_AUTHENTICATION_REALMS, Serializer.json(realm), response -> {
            if (response.succeeded()) {
                UpdateResponse update = new UpdateResponse(response.result());

                if (update.is(ACCEPTED)) {
                    if (!registered) {
                        context.onRealmRegistered(context.realm().getId());
                    }
                    registered = true;
                } else {
                    registered = false;
                    context.onRealmRejected(context.realm().getId(), update.message());
                }
            }
        });
    }

    @Override
    public String address() {
        return context.realm().getId();
    }

    @Override
    public void handle(Request request) {
        protocol.process(request);
    }
}
