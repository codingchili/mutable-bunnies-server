package com.codingchili.realm.instance.transport;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * @author Robin Duda
 *
 * A codec to forward requests over the local event bus, avoiding unnecessary deserialization
 * by just passing the reference.
 */
public class ControlMessageCodec<Request> implements MessageCodec<Request, Request> {

    @Override
    public void encodeToWire(Buffer buffer, Request request) {
        throw new UnsupportedOperationException("Local codec: Cannot encode requests to the wire.");
    }

    @Override
    public Request decodeFromWire(int i, Buffer buffer) {
        throw new UnsupportedOperationException("Local codec: Cannot decode requests from the wire.");
    }

    @Override
    public Request transform(Request request) {
        return request;
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
