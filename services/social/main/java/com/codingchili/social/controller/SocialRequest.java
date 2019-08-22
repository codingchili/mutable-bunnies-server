package com.codingchili.social.controller;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 * <p>
 * Request wrapper for the social service.
 */
public class SocialRequest implements RequestWrapper {
    private static final String ID_FRIEND = "friend";
    private Request request;

    public SocialRequest(Request request) {
        this.request = request;
    }

    /**
     * Deserializes the request into the given class.
     *
     * @param theClass the target class type.
     * @param <T>      generic type inference.
     * @return the deserialized object.
     */
    public <T> T raw(Class<T> theClass) {
        return Serializer.unpack(data(), theClass);
    }

    public String account() {
        return request.token().getDomain();
    }

    public String friend() {
        return data().getString(ID_FRIEND);
    }

    @Override
    public Request request() {
        return request;
    }
}
