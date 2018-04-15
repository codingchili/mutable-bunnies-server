package com.codingchili.realm.controller;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 */
public class RealmRequest extends RequestWrapper {

    RealmRequest(Request request) {
        super(request);
    }

    public String account() {
        return connection().getProperty(ID_ACCOUNT).orElseThrow(() ->
                new CoreRuntimeException("Account name is required from unauthenticated route."));
    }

    public String character() {
        return data().getString(ID_CHARACTER);
    }

    public String className() {
        return data().getString(ID_PLAYERCLASS);
    }

    public String instance() {
        return data().getString(ID_INSTANCE);
    }

    public String connected() {
        return connection().getProperty(ID_INSTANCE).orElseThrow(() ->
                new CoreRuntimeException("Failed to retrieve instance from connection: not set."));
    }
}
