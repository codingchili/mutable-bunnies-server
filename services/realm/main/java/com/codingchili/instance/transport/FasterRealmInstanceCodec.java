package com.codingchili.instance.transport;

import com.codingchili.instance.model.entity.EventProtocol;
import com.codingchili.instance.model.entity.PlayerCreature;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.logging.RemoteLogger;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 * <p>
 * A codec to forward requests over the local event bus, avoiding unnecessary deserialization
 * by just passing the reference.
 */
public class FasterRealmInstanceCodec<S, R> implements MessageCodec<S, R> {
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final String codecName = FasterRealmInstanceCodec.class.getSimpleName();

    /**
     * Initializes codecs used for realm-instance communication.
     *
     * @param context the core context that contains the event bus to register codecs on.
     */
    public static void initialize(CoreContext context) {
        if (!initialized.getAndSet(true)) {
            context.bus().registerCodec(new FasterRealmInstanceCodec<>());
        }

        /*Consumer<Class<?>> register = (theClass) -> {
            context.bus().registerDefaultCodec(theClass, new FasterRealmInstanceCodec<>(theClass));
        };

        if (!initialized.getAndSet(true)) {
            //register.accept(ReceivableMessage.class);
        }*/
    }

    @Override
    public void encodeToWire(Buffer buffer, S message) {
        // when encoding to wire: encode as json object.
        buffer.writeToBuffer(Serializer.buffer(message));
    }

    @Override
    @SuppressWarnings("unchecked")
    public R decodeFromWire(int i, Buffer buffer) {
        // when decoding from wire: decode as json object and map to source class.
        return (R) buffer.toJsonObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public R transform(S message) {
        return (R) message;//Serializer.json(message);
    }

    @Override
    public String name() {
        return codecName;
    }

    public static String getName() {
        return codecName;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
