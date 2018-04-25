package com.codingchili.realm.controller;

import com.codingchili.realm.instance.transport.FasterRealmInstanceCodec;
import com.codingchili.realm.instance.transport.InstanceRequest;
import io.vertx.core.Future;

import java.util.function.Supplier;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;

/**
 * @author Robin Duda
 * <p>
 * Local event bus listener - uses direct references to avoid serialization when
 * passing objects between the realm and the instance. This only works while the
 * instances and realm server are deployed on the same server. This is a bit faster
 * as it avoids passing messages on the cluster, but may fail in the future because
 * we cannot scale out instances using the LocalBusListener. According to my projections
 * this will not be an issue within at least 10 years, where we have coded more instances.
 */
public class FasterBusListener implements CoreListener {
    private CoreHandler handler;
    private CoreContext core;

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public void start(Future<Void> start) {
        FasterRealmInstanceCodec.initialize(core);

        RequestProcessor processor = new RequestProcessor(core, handler);
        core.bus().consumer(handler.address(), msg -> {
            // wrap in a new RequestMessage to support using the event bus for request-response
            // and to avoid handling failures in multiple channels (bus + direct request reference).
            processor.submit(() -> new InstanceRequest(msg));
        });
        handler.start(start);
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }

    @Override
    public CoreListener settings(Supplier<ListenerSettings> settings) {
        // no settings required.
        return this;
    }

    @Override
    public CoreListener handler(CoreHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
