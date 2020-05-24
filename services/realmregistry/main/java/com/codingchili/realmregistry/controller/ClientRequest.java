package com.codingchili.realmregistry.controller;

import com.codingchili.common.Strings;
import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.ID_TOKEN;

/**
 * @author Robin Duda
 */
class ClientRequest implements RequestWrapper {
    private Request request;

    ClientRequest(Request request) {
        this.request = request;
    }

    public String realmId() {
        return data().getString(Strings.ID_REALM);
    }

    public String account() {
        return token().getDomain();
    }

    @Override
    public Request request() {
        return request;
    }

    public Token token() {
        if (data().containsKey(ID_TOKEN)) {
            return Serializer.unpack(data().getJsonObject(ID_TOKEN), Token.class);
        } else {
            return new Token().setExpiry(0);
        }
    }
}
