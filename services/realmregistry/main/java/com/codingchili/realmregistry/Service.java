package com.codingchili.realmregistry;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.realmregistry.configuration.RegistryContext;
import com.codingchili.realmregistry.controller.RealmRegistryClientHandler;
import com.codingchili.realmregistry.controller.RealmRegistryHandler;
import io.vertx.core.*;

import static com.codingchili.core.context.FutureHelper.untyped;


/**
 * @author Robin Duda
 * Starts up the client handler and the realmName handler.
 */
public class Service implements CoreService {
    private RegistryContext context;
    private CoreContext core;

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public void start(Promise<Void> start) {
        context = new RegistryContext(core);

        CompositeFuture.all(
                context.handler(() -> new RealmRegistryHandler(context)),
                context.handler(() -> new RealmRegistryClientHandler(context))
        ).onComplete(untyped(start));
    }
}
