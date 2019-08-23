package com.codingchili.instance.transport;

import com.codingchili.common.ReceivableMessage;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.transport.Connection;
import com.codingchili.core.protocol.Response;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * <p>
 * A message to an instance that a player is joining.
 */
public class InstanceRequest implements Request {
    private ReceivableMessage reference;
    private Message<Object> message;
    private JsonObject data;

    /**
     * Constructor for receiving a RequestMessage over the cluster.
     *
     * @param message a message received over the cluster.
     */
    public InstanceRequest(Message<Object> message) {
        this.message = message;

        // force unpack of request when messages are received over the cluster.
        if (message.body() instanceof ReceivableMessage) {
            this.reference = (ReceivableMessage) message.body();
        } else if (message.body() instanceof JsonObject) {
            this.data = (JsonObject) message.body();
        } else {
            throw new CoreRuntimeException("Unsupported message object: " + message.body().getClass().getName());
        }
    }

    @Override
    public void write(Object object) {
        message.reply(Response.json(target(), route(), object));
    }

    @Override
    public JsonObject data() {
        if (reference != null && data == null) {
            this.data = Serializer.json(reference);
            data.put(PROTOCOL_ROUTE, route());
            data.put(PROTOCOL_TARGET, target());
        }
        return data;
    }

    @Override
    public String route() {
        if (reference != null) {
            return reference.route();
        } else {
            return data.getString(PROTOCOL_ROUTE);
        }
    }

    @Override
    public String target() {
        if (reference != null) {
            return reference.target();
        } else {
            return data.getString(PROTOCOL_TARGET);
        }
    }

    @Override
    public int size() {
        // not applicable for internal requests.
        return 0;
    }

    @Override
    public Connection connection() {
        throw new CoreRuntimeException("Event bus messages does not support connections.");
    }

    /**
     * Deserializes the data of this request into the given target class. If
     * this request is local then the given reference will just be casted.
     *
     * @param theClass the class to represent the request data as.
     * @param <T>      the class type parameter.
     * @return a deserialized or casted object to the given type from the requests data.
     */
    public <T> T raw(Class<T> theClass) {
        if (reference != null && theClass.isAssignableFrom(reference.getClass())) {
            return theClass.cast(reference);
        } else {
            return Serializer.unpack(data(), theClass);
        }
    }
}
