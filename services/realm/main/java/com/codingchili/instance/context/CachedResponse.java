package com.codingchili.instance.context;

import io.vertx.core.buffer.Buffer;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.Response;

/**
 * @author Robin Duda
 *
 * Utility class for pre-generating responses. Very efficient
 * when sending large json objects to clients.
 */
public class CachedResponse {

    /**
     * Creates a pre-serialized response message with the accepted
     * status. This can be used for objects that are large and rarely change.
     * For example the spells database, to serialize it on each request is
     * wasteful.
     *
     * @param route  the route that will be placed in the response header.
     * @param object the object to add headers to and precompile a response for.
     *               cannot be a #{@link Buffer} because then you are doing something wrong.
     * @return a buffer, ready to be sent over the wire efficiently.
     */
    public static Buffer make(String route, Object object) {
        if (object instanceof Buffer) {
            throw new CoreRuntimeException("Cannot create cached response from a " + object.getClass().getName());
        }
        return Response.buffer(null, route, object);
    }
}
