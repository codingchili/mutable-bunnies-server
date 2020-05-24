package com.codingchili.realmregistry.controller;

import com.codingchili.common.RegisteredRealm;
import com.codingchili.common.Strings;
import com.codingchili.realmregistry.configuration.RegistryContext;
import com.codingchili.realmregistry.model.*;
import io.vertx.core.Future;

import java.time.Instant;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.*;

import static com.codingchili.common.Strings.NODE_AUTHENTICATION_REALMS;

/**
 * @author Robin Duda
 * Routing used to authenticate realms and generate realm lists.
 */
public class RealmRegistryHandler implements CoreHandler {
    private final Protocol<RealmRequest> protocol = new Protocol<>();
    private AsyncRealmStore realms;
    private RegistryContext context;

    public RealmRegistryHandler(RegistryContext context) {
        this.context = context;

        protocol.authenticator(this::authenticate)
                .use(Strings.REALM_UPDATE, this::update)
                .use(Strings.CLIENT_CLOSE, this::disconnected)
                .use(CoreStrings.ID_PING, Request::accept, Role.PUBLIC);
    }

    @Override
    public void start(Future<Void> start) {
        context.getRealmStore(done -> {
           if (done.succeeded()) {
                this.realms = done.result();
                start.complete();
           } else {
               start.fail(done.cause());
           }
        });
    }

    @Override
    public void handle(Request request) {
        protocol.process(new RealmRequest(request));
    }

    private Future<RoleType> authenticate(Request request) {
        Future<RoleType> future = Future.future();
        context.verifyRealmToken(request.token()).setHandler(authenticated -> {
           if (authenticated.succeeded()) {
               future.complete(Role.USER);
           }  else {
               future.complete(Role.PUBLIC);
           }
        });
        return future;
    }

    private void update(RealmRequest request) {
        RegisteredRealm realm = request.getRealm();
        realm.setTrusted(context.isTrustedRealm(realm.getId()));
        realm.setUpdated(Instant.now().toEpochMilli());
        realm.setPlayers(request.players());

        realms.put(insert -> {
            if (insert.succeeded()) {
                request.accept();
                context.onRealmUpdated(realm.getId(), realm.getPlayers());
            } else {
                request.error(new RealmUpdateException());
            }
        }, realm);
    }

    private void disconnected(RealmRequest request) {
        realms.remove(remove -> {
            if (remove.succeeded()) {
                request.accept();
            } else {
                request.error(new RealmDisconnectException());
            }
        }, request.realmName());
    }

    @Override
    public String address() {
        return NODE_AUTHENTICATION_REALMS;
    }
}