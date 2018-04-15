package com.codingchili.realm.instance.transport;

import com.codingchili.realm.instance.controller.InstanceRequest;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * @author Robin Duda
 *
 * Codec for incoming messages from a player to the realm server.
 */
public class PlayerRequestCodec implements MessageCodec<InstanceRequest, InstanceRequest> {

    @Override
    public void encodeToWire(Buffer buffer, InstanceRequest instance) {
        throw new UnsupportedOperationException("Local codec: Cannot encode requests to the wire.");
    }

    @Override
    public InstanceRequest decodeFromWire(int i, Buffer buffer) {
        throw new UnsupportedOperationException("Local codec: Cannot encode requests to the wire.");
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

    @Override
    public InstanceRequest transform(InstanceRequest instance) {
        return instance;
    }
}
