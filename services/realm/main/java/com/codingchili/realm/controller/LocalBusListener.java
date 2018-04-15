package com.codingchili.realm.controller;

import com.codingchili.realm.instance.controller.InstanceRequest;
import com.codingchili.realm.instance.transport.*;
import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;

/**
 * @author Robin Duda
 *
 * Local event bus listener - uses direct references to avoid serialization when
 * passing objects between the realm and the instance. This only works while the
 * instances and realm server are deployed on the same server. This is a bit faster
 * as it avoids passing messages on the cluster, but may fail in the future because
 * we cannot scale out instances using the LocalBusListener. According to my projections
 * this will not be an issue within at least 10 years, where we have coded more instances.
 */
public class LocalBusListener implements CoreListener {
    private static AtomicBoolean registered = new AtomicBoolean(false);
    private ListenerSettings settings;
    private CoreHandler handler;
    private CoreContext core;

    @Override
    public void start(Future<Void> start) {
        if (!registered.getAndSet(true)) {
            core.bus().registerDefaultCodec(ControlRequest.class, new ControlMessageCodec<>());
            core.bus().registerDefaultCodec(PlayerRequest.class, new PlayerMessageCodec<>());
            core.bus().registerDefaultCodec(InstanceRequest.class, new PlayerRequestCodec());
        }

        RequestProcessor processor = new RequestProcessor(core, handler);
        core.bus().localConsumer(handler.address(), msg -> {
            processor.submit(() -> (Request) msg.body());
        });
        handler.start(start);
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public CoreListener settings(Supplier<ListenerSettings> settings) {
        this.settings = settings.get();
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
