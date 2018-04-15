package com.codingchili.realm.instance.controller;

import com.codingchili.realm.controller.RealmRequest;
import com.codingchili.realm.instance.model.entity.PlayerCreature;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;

import static com.codingchili.common.Strings.ID_ACCOUNT;
import static com.codingchili.core.configuration.CoreStrings.ID_NAME;

/**
 * @author Robin Duda
 *
 * A message to an instance that a player is joining.
 */
public class InstanceRequest extends RequestWrapper {
    private String account;
    private String player;

    /**
     * Constructor for inbound messages.
     * @param request the request to map as an instance request.
     */
    public InstanceRequest(RealmRequest request) {
        super(request);
        this.player = request.connection().getProperty(ID_NAME).orElseThrow(() ->
                new CoreRuntimeException("Connection is missing character name."));
        this.account = request.connection().getProperty(ID_ACCOUNT).orElseThrow(() ->
                new CoreRuntimeException("Connection is missing account name"));
    }

    public String character() {
        return player;
    }


    public String account() {
        return account;
    }
}
