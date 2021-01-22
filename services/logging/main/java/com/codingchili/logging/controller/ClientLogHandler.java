package com.codingchili.logging.controller;

import com.codingchili.core.listener.Request;
import com.codingchili.core.logging.LogLevel;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.security.Token;
import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.logging.Level;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 * <p>
 * Log handler for messages incoming from clients.
 */
public class ClientLogHandler extends AbstractLogHandler {

    public ClientLogHandler(LogContext context) {
        super(context, NODE_CLIENT_LOGGING);
    }

    @Override
    protected void logging(Request request) {
        JsonObject logdata = request.data().getJsonObject(ID_MESSAGE);
        // force event to known good value, clients are only allowed to specify client.event
        // block changes to timestamp
        logdata.put("level", Level.INFO.getName());
        logdata.put("source", "Browser");

        verifyToken(request.data()).setHandler(verify -> {
            if (verify.succeeded()) {
                logdata.remove(ID_TOKEN);
                console.log(logdata);
                store.log(logdata.copy());
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
}
