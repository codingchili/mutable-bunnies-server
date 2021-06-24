package com.codingchili.router;

import com.codingchili.core.Launcher;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.WireType;
import com.codingchili.core.listener.transport.RestListener;
import com.codingchili.core.listener.transport.TcpListener;
import com.codingchili.core.listener.transport.UdpListener;
import com.codingchili.core.listener.transport.WebsocketListener;
import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.controller.RouterHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.codingchili.core.context.FutureHelper.untyped;
import static io.vertx.core.CompositeFuture.all;

/**
 * @author Robin Duda
 * root game server, deploys realmName servers.
 */
public class Service implements CoreService {
    private RouterContext context;
    private RouterHandler handler;

    public Service() {
    }

    public Service(RouterContext context) {
        this.context = context;
    }

    @Override
    public void init(CoreContext core) {
        if (context == null) {
            this.context = new RouterContext(core);
        }
    }

    @Override
    public void start(Promise<Void> start) {
        context.blocking(blocking -> {
            List<Future> deployments = new ArrayList<>();

            for (ListenerSettings listener : context.transports()) {
                handler = new RouterHandler(context);
                Promise<String> promise = Promise.promise();
                deployments.add(promise.future());

                switch (listener.getType()) {
                    case UDP:
                        start(UdpListener::new, listener.getType(), promise);
                        break;
                    case TCP:
                        start(TcpListener::new, listener.getType(), promise);
                        break;
                    case WEBSOCKET:
                        start(WebsocketListener::new, listener.getType(), promise);
                        break;
                    case REST:
                        start(RestListener::new, listener.getType(), promise);
                        break;
                }
            }
            all(deployments).onComplete(untyped(blocking));
        }, start);
    }

    private void start(Supplier<CoreListener> listener, WireType type, Promise<String> future) {
        context.listener(() -> listener.get()
                .handler(handler)
                .settings(context.getListener(type)))
                .onComplete(future);
    }

    public static void main(String[] args) {
        Launcher.main(args);
    }
}
