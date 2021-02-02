package com.codingchili.logging.controller;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.protocol.Api;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Roles;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.model.StorageLogger;
import com.codingchili.logging.statistics.CharacterCreate;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.RoleMap.PUBLIC;


/**
 * @author Robin Duda
 * <p>
 * Log handler for log messages incoming from services.
 */
@Roles(PUBLIC)
public class ServiceLogHandler implements CoreHandler {
    private Protocol<Request> protocol = new Protocol<>(this);
    private ConsoleLogger console;
    private StorageLogger store;
    private LogContext context;

    /**
     *
     * @param context
     */
    public ServiceLogHandler(LogContext context) {
        this.context = context;
        this.console = new ConsoleLogger(getClass());
        this.store = new StorageLogger(context, getClass());
    }

    @Api
    public void ping(Request request) {
        request.accept();
    }

    @Api
    protected void logging(Request request) {
        JsonObject logdata = request.data().getJsonObject(ID_MESSAGE);
        String node = logdata.getString(LOG_NODE);

        if (!NODE_LOGGING.equals(node) && context.consoleEnabled()) {
            console.log(logdata.copy());
        }
        //process(logdata);
        store.log(logdata);
    }

    private void process(JsonObject logdata) {
        // capture filters for in memory stats.
        if (logdata.getString(LOG_EVENT).equals(CLIENT_CHARACTER_CREATE)) {
            var create = Serializer.unpack(logdata, CharacterCreate.class);
        }
    }

    // implement when logger metadata supports complex objects.
    private Future<Void> verifyToken(JsonObject logdata) {
        if (logdata.containsKey(ID_TOKEN)) {
            return context.verifyServerToken(Serializer.unpack(logdata.getJsonObject(ID_TOKEN), Token.class));
        } else {
            return Future.failedFuture("Request is missing token.");
        }
    }

    @Override
    public void handle(Request request) {
        protocol.process(request);
    }

    @Override
    public String address() {
        return NODE_LOGGING;
    }
}
