package com.codingchili.realm.instance.transport;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.Connection;
import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 * <p>
 * Control requests may be passed between the realm and the instance.
 * When a request is passed it is done so by reference, this is supported
 * only for local transmissions.
 */
public class ControlRequest implements Request {
    private ControlMessage message;
    private Future<Object> future;

    /**
     * Creates a new control request.
     *
     * @param future  completed when the requested operation completes at the receiver.
     * @param message the control message to send.
     */
    public ControlRequest(Future<Object> future, ControlMessage message) {
        this.message = message;
        this.future = future;
    }

    @Override
    public String route() {
        // this is implemented to avoid having to call data() to retrieve the route.
        return message.getRoute();
    }

    @Override
    public String target() {
        // this is implemented to avoid having to call data() to retrieve the target.
        return message.getTarget();
    }

    @Override
    public Connection connection() {
        throw new UnsupportedOperationException("No connections can be created from a LocalRequest.");
    }

    /**
     * @return prefer calling this method instead of #{@link #data()} as this avoids serialization.
     */
    public <T> T raw(Class<T> toCast) {
        return toCast.cast(message);
    }

    @Override
    public JsonObject data() {
        // this type of message should never need to be serialized as it
        // should always be passed locally as a reference.
        throw new CoreRuntimeException("Warning: attempted to call Request#data() on local reference.");
    }

    @Override
    public int size() {
        // for local messages we consider the size to be 0.
        return 0;
    }

    @Override
    public void write(Object object) {
        future.complete(object);
    }
}
