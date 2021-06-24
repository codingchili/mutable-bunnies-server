package com.codingchili.authentication.controller;

import com.codingchili.authentication.configuration.AuthContext;
import com.codingchili.authentication.model.*;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.*;
import com.codingchili.core.security.Account;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.Role.PUBLIC;

/**
 * @author Robin Duda
 * Routing used to register/authenticate accounts.
 */
public class ClientHandler implements CoreHandler {
    private final Protocol<ClientLogin> protocol = new Protocol<>();
    private final AsyncAccountStore accounts;
    private AuthContext context;

    public ClientHandler(AuthContext context) {
        this.context = context;

        accounts = context.getAccountStore();

        protocol.authenticator(this::authenticate)
                .use(CLIENT_REGISTER, this::register, PUBLIC)
                .use(CLIENT_AUTHENTICATE, this::authenticate, PUBLIC)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    @Override
    public void handle(Request request) {
        protocol.process(new ClientLogin(request));
    }

    private Future<RoleType> authenticate(Request request) {
        Promise<RoleType> promise = Promise.promise();
        context.verifyClientToken(request.token()).onComplete(verify -> {
            if (verify.succeeded()) {
                promise.complete(Role.USER);
            } else {
                promise.complete(Role.PUBLIC);
            }
        });
        return promise.future();
    }

    private void register(ClientLogin request) {
        accounts.register(register -> {
            if (register.succeeded()) {
                sendAuthentication(register.result(), request, true);
            } else {
                request.error(register.cause());
            }
        }, request.getAccount());
    }

    private void authenticate(ClientLogin request) {
        accounts.authenticate(authentication -> {
            if (authentication.succeeded()) {
                sendAuthentication(authentication.result(), request, false);
            } else {
                request.error(authentication.cause());

                if (authentication.cause() instanceof AccountPasswordException)
                    context.onAuthenticationFailure(request.getAccount(), request.remote());
            }
        }, request.getAccount());
    }

    private void sendAuthentication(Account account, ClientLogin request, boolean registered) {
        context.signClientToken(account.getUsername()).onComplete(sign -> {
            request.write(
                    new ClientAuthentication(
                            account,
                            sign.result(),
                            registered));

            if (registered)
                context.onRegistered(account.getUsername(), request.remote());
            else
                context.onAuthenticated(account.getUsername(), request.remote());
        });
    }

    @Override
    public String address() {
        return NODE_AUTHENTICATION_CLIENTS;
    }
}