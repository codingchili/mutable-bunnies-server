package com.codingchili.realm.instance.transport;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * @author Robin Duda
 *
 * Optimized codec for sending local messages from an instance to the realm, and then a player.
 */
public class PlayerMessageCodec<PlayerMessage> implements MessageCodec<PlayerMessage, PlayerMessage> {

    @Override
    public void encodeToWire(Buffer buffer, PlayerMessage msg) {
        throw new UnsupportedOperationException("Local codec: Cannot encode requests to the wire.");
    }

    @Override
    public PlayerMessage decodeFromWire(int i, Buffer buffer) {
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
    public PlayerMessage transform(PlayerMessage message) {
        return message;
    }
}
