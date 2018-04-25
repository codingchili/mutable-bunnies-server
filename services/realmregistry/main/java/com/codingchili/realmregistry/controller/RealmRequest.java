package com.codingchili.realmregistry.controller;

import com.codingchili.common.RegisteredRealm;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * <p>
 * A request from the raelm to the realm registry.
 */
class RealmRequest implements RequestWrapper {
    private RegisteredRealm realm;
    private Request request;

    RealmRequest(Request request) {
        this.request = request;

        if (data().containsKey(ID_REALM)) {
            realm = Serializer.unpack(data().getJsonObject(ID_REALM), RegisteredRealm.class);
        }

        if (data().containsKey(ID_TOKEN)) {
            realm.setAuthentication(Serializer.unpack(data().getJsonObject(ID_TOKEN), Token.class));
        }
    }

    @Override
    public Request request() {
        return request;
    }

    public Token token() {
        return realm.getAuthentication();
    }

    public RegisteredRealm getRealm() {
        return realm;
    }

    public String realmName() {
        return token().getDomain();
    }

    public int players() {
        if (data().containsKey(ID_PLAYERS)) {
            return data().getInteger(ID_PLAYERS);
        } else {
            return 0;
        }
    }
}
