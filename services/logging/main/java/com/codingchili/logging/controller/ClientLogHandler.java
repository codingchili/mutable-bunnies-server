package com.codingchili.logging.controller;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.protocol.Api;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Roles;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.security.Token;
import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.model.StorageLogger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.logging.Level;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.RoleMap.PUBLIC;


/**
 * @author Robin Duda
 * <p>
 * Log handler for messages incoming from clients.
 */
@Roles(PUBLIC)
public class ClientLogHandler implements CoreHandler {
    private Protocol<Request> protocol = new Protocol<>(this);
    private static final String BROWSER = "browser";
    private LogContext context;
    private ConsoleLogger console;
    private StorageLogger store;

    /**
     *
     * @param context
     */
    public ClientLogHandler(LogContext context) {
        this.context = context;
        this.console = new ConsoleLogger(getClass());
        this.store = new StorageLogger(context, getClass());
    }

    @Api
    public void ping(Request request) {
        request.accept();
    }

    @Api
    public void logging(Request request) {
        JsonObject logdata = request.data().getJsonObject(ID_MESSAGE);
        // clients are not allowed to overwrite the following values.
        logdata.put(LOG_LEVEL, Level.INFO.getName());
        logdata.put(LOG_SOURCE, BROWSER);
        logdata.put(ID_ACCOUNT, request.token().getDomain());
        logdata.put(LOG_TIME, Instant.now().toEpochMilli());
        logdata.put(LOG_REMOTE, request.data().getString(PROTOCOL_CONNECTION));

        verifyToken(request.data()).onComplete(verify -> {
            if (verify.succeeded()) {
                console.log(logdata.copy());
                store.log(logdata);
                request.accept();
            } else {
                request.error(new AuthorizationRequiredException());
            }
        });
    }

    private Future<Void> verifyToken(JsonObject logdata) {
        if (logdata.containsKey(ID_TOKEN)) {
            return context.verifyClientToken(Serializer.unpack(logdata.getJsonObject(ID_TOKEN), Token.class));
        } else {
            return Future.failedFuture("request is missing token.");
        }
    }

    @Override
    public void handle(Request request) {
        protocol.process(request);
    }

    @Override
    public String address() {
        return NODE_CLIENT_LOGGING;
    }
}
