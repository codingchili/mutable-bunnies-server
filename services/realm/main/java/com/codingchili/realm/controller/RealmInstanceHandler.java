package com.codingchili.realm.controller;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.instance.context.InstanceContext;
import com.codingchili.instance.context.InstanceSettings;
import com.codingchili.instance.controller.InstanceHandler;
import com.codingchili.instance.model.events.SavePlayerMessage;
import com.codingchili.instance.transport.InstanceRequest;
import com.codingchili.realm.model.RealmUpdate;
import com.codingchili.realm.model.UpdateResponse;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.*;
import com.codingchili.core.listener.transport.Connection;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.*;

import static com.codingchili.common.Strings.NODE_AUTHENTICATION_REALMS;
import static com.codingchili.core.configuration.CoreStrings.ANY;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;
import static com.codingchili.core.protocol.RoleMap.PUBLIC;

/**
 * @author Robin Duda
 *
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
        context.periodic(context::updateRate, getClass().getSimpleName(), this::registerRealm);
        this.registerRealm(-1L);
    }

    @Override
    public void start(Future<Void> future) {
        context.onRealmStarted(context.realm().getNode());
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
    public void save(InstanceRequest request) {
        SavePlayerMessage message = request.raw(SavePlayerMessage.class);
        context.characters().update(message.getCreature()).setHandler(request::result);
    }

    private void deployInstances(Future<Void> future) {
        List<Future> futures = new ArrayList<>();
        for (InstanceSettings instance : context.instances()) {
            Future deploy = Future.future();
            futures.add(deploy);

            context.blocking((blocking) -> {
                InstanceContext instanceContext = new InstanceContext(context, instance);
                InstanceHandler handler = new InstanceHandler(instanceContext);

                // sometime in the future the instances will be deployed remotely - just deploy
                // the instances on the same cluster.
                instanceContext.listener(() -> new FasterBusListener().handler(handler)).setHandler((done) -> {
                    if (done.succeeded()) {
                        deploy.complete();
                        blocking.complete();
                    } else {
                        context.onInstanceFailed(instance.getName(), done.cause());
                        deploy.fail(done.cause());
                        blocking.complete();
                    }
                });
            }, (done) -> {});
        }
        CompositeFuture.all(futures).setHandler(done -> {
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

        context.bus().send(NODE_AUTHENTICATION_REALMS, Serializer.json(realm), response -> {
            if (response.succeeded()) {
                UpdateResponse update = new UpdateResponse(response.result());

                if (update.is(ACCEPTED)) {
                    if (!registered) {
                        context.onRealmRegistered(context.realm().getNode());
                    }
                    registered = true;
                } else {
                    registered = false;
                    context.onRealmRejected(context.realm().getNode(), update.message());
                }
            }
        });
    }

    @Override
    public String address() {
        return context.realm().getNode();
    }

    @Override
    public void handle(Request request) {
        protocol.process(request);
    }
}
