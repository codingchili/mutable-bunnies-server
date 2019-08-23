package com.codingchili.social.controller;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.common.Strings.ID_REALM;
import static com.codingchili.core.configuration.CoreStrings.ID_MESSAGE;

/**
 * @author Robin Duda
 * <p>
 * Request wrapper for the social service.
 */
public class SocialRequest implements RequestWrapper {
    private static final String ID_FRIEND = "friend";
    private static final String ID_PARTY = "party";
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

    public String party() {
        return request.data().getString(ID_PARTY);
    }

    public String friend() {
        return data().getString(ID_FRIEND);
    }

    public String message() {
        return data().getString(ID_MESSAGE);
    }

    public String realm() {
        return data().getString(ID_REALM);
    }

    @Override
    public Request request() {
        return request;
    }
}
