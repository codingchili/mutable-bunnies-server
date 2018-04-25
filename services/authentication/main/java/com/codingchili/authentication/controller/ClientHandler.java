package com.codingchili.authentication.controller;

import com.codingchili.authentication.configuration.AuthContext;
import com.codingchili.authentication.model.AccountPasswordException;
import com.codingchili.authentication.model.AsyncAccountStore;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Role;
import com.codingchili.core.security.Account;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.Role.PUBLIC;
import static com.codingchili.core.protocol.Role.USER;

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

        protocol.use(CLIENT_REGISTER, this::register, PUBLIC)
                .use(CLIENT_AUTHENTICATE, this::authenticate, PUBLIC)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    @Override
    public void handle(Request request) {
        Role role = (context.verifyClientToken(request.token())) ? USER : PUBLIC;
        protocol.get(request.route(), role).submit(new ClientLogin(request));
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
        request.write(
                new com.codingchili.authentication.model.ClientAuthentication(
                        account,
                        context.signClientToken(account.getUsername()),
                        registered));

        if (registered)
            context.onRegistered(account.getUsername(), request.remote());
        else
            context.onAuthenticated(account.getUsername(), request.remote());
    }

    @Override
    public String address() {
        return NODE_AUTHENTICATION_CLIENTS;
    }
}