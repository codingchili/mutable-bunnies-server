package com.codingchili.logging.controller;

import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 * <p>
 * Log handler for log messages incoming from services.
 */
public class ServiceLogHandler extends AbstractLogHandler {

    public ServiceLogHandler(LogContext context) {
        super(context, NODE_LOGGING);
    }

    @Override
    protected void logging(Request request) {
        /*verifyToken(request.data()).setHandler(verify -> {
            if (verify.succeeded()) {*/
                JsonObject logdata = request.data().getJsonObject(ID_MESSAGE);
                String node = logdata.getString(LOG_NODE);

                if (!NODE_LOGGING.equals(node) && context.consoleEnabled()) {
                    console.log(logdata.copy());
                }
                store.log(logdata);
            /*} else {
                request.error(new AuthorizationRequiredException());
            }
        });*/
    }

    // implement when logger metadata supports complex objects.
    private Future<Void> verifyToken(JsonObject logdata) {
        if (logdata.containsKey(ID_TOKEN)) {
            return context.verifyServerToken(Serializer.unpack(logdata.getJsonObject(ID_TOKEN), Token.class));
        } else {
            return Future.failedFuture("Request is missing token.");
        }
    }
}
