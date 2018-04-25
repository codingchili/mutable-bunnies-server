package com.codingchili.authentication.controller;


import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Account;

import static com.codingchili.common.Strings.ID_ACCOUNT;
import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_CONNECTION;

/**
 * @author Robin Duda
 * <p>
 * Client authentication request.
 */
class ClientLogin implements RequestWrapper {
    private Request request;

    ClientLogin(Request request) {
        this.request = request;
    }

    public Account getAccount() {
        return Serializer.unpack(data().getJsonObject(ID_ACCOUNT), Account.class);
    }

    @Override
    public Request request() {
        return request;
    }

    public String remote() {
        return data().getString(PROTOCOL_CONNECTION);
    }
}
