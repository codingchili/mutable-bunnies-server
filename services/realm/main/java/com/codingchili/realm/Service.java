package com.codingchili.realm;

import com.codingchili.common.Strings;
import com.codingchili.instance.context.InstancesBootstrap;
import com.codingchili.realm.configuration.*;
import com.codingchili.realm.controller.*;
import com.codingchili.realm.model.RealmNotUniqueException;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.listener.transport.WebsocketListener;

import static com.codingchili.common.Strings.PATH_REALM;
import static com.codingchili.core.configuration.CoreStrings.EXT_YAML;
import static com.codingchili.core.context.FutureHelper.untyped;
import static com.codingchili.core.files.Configurations.system;
import static com.codingchili.realm.configuration.RealmServerSettings.PATH_REALMSERVER;

/**
 * @author Robin Duda
 * root game server, deploys realmName servers.
 */
public class Service implements CoreService {
    private RealmServerContext context;

    private static JsonObject getPing() {
        return new JsonObject()
                .put(Strings.PROTOCOL_ROUTE, Strings.ID_PING);
    }

    private static DeliveryOptions getDeliveryOptions() {
        return new DeliveryOptions()
                .setSendTimeout(system().getDeployTimeout());
    }

    @Override
    public void init(CoreContext core) {
        this.context = new RealmServerContext(core);
    }

    @Override
    public void start(Future<Void> start) {
        RealmServerSettings server = Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);
        List<Future> deployments = new ArrayList<>();

        InstancesBootstrap.bootstrap(context).setHandler(done -> {
            if (done.succeeded()) {
                for (String enabled : server.getEnabled()) {
                    Supplier<RealmSettings> realm = () -> Configurations.get(realmPath(enabled), RealmSettings.class);
                    realm.get().load();
                    Future<Void> future = Future.future();
                    deploy(future, realm);
                    deployments.add(future);
                }
                CompositeFuture.all(deployments).setHandler(untyped(start));
            } else {
                start.fail(done.cause());
            }
        });
    }

    private static String realmPath(String realmName) {
        return PATH_REALM + realmName + EXT_YAML;
    }

    /**
     * Dynamically deploy a new realm, verifies that no existing nodes are already listening
     * on the same address by sending a ping.
     *
     * @param realm the realm to be deployed dynamically.
     */
    private void deploy(Future<Void> future, Supplier<RealmSettings> realm) {
        Consumer<RealmContext> deployer = (rc) -> {
            // Check if the routing id for the realm is unique
            context.bus().send(realm.get().getNode(), getPing(), getDeliveryOptions(), response -> {

                if (response.failed()) {
                    // If no response then the id is not already in use.
                    CoreListener listener = new WebsocketListener()
                            .settings(realm.get().getListener())
                            .handler(new RealmClientHandler(rc));

                    // deploy handler for incoming messages from instances.
                    rc.listener(() -> new FasterBusListener().handler(new RealmInstanceHandler(rc)))
                            .setHandler(instances -> {

                                if (instances.succeeded()) {
                                    // deploy handler for incoming messages from clients.
                                    rc.listener(() -> listener).setHandler(deploy -> {
                                        if (deploy.failed()) {
                                            rc.onDeployRealmFailure(realm.get().getNode());
                                            throw new RuntimeException(deploy.cause());
                                        }
                                    }).setHandler(clients -> {
                                        if (clients.succeeded()) {
                                            future.complete();
                                        } else {
                                            future.fail(clients.cause());
                                        }
                                    });
                                } else {
                                    future.fail(instances.cause());
                                }
                            });

                } else {
                    future.fail(new RealmNotUniqueException());
                }
            });
        };

        // set up the realm context asynchronously.
        RealmContext.create(context, realm).setHandler(create -> {
            if (create.succeeded()) {
                deployer.accept(create.result());
            } else {
                future.fail(new RuntimeException(create.cause()));
            }
        });
    }
}
