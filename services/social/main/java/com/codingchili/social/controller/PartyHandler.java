package com.codingchili.social.controller;


import com.codingchili.social.configuration.SocialContext;
import com.codingchili.social.model.PartyEngine;
import com.codingchili.social.model.PartyListResponse;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 * <p>
 * Player parties.
 */
public class PartyHandler implements SocialServiceHandler {
    private PartyEngine engine;

    /**
     * @param context the context to run the handler on.
     */
    public PartyHandler(SocialContext context) {
        this.engine = context.party();
    }

    @Api
    public void party_list(SocialRequest request) {
        request.write(new PartyListResponse(engine.list(request.account())));
    }

    @Api
    public void party_invite(SocialRequest request) {
        engine.invite(request.account(), request.friend());
        request.accept();
    }

    @Api
    public void party_leave(SocialRequest request) {
        engine.leave(request.account());
        request.accept();
    }

    @Api
    public void party_accept(SocialRequest request) {
        engine.accept(request.account(), request.party());
        request.accept();
    }

    @Api
    public void party_decline(SocialRequest request) {
        engine.decline(request.account(), request.party());
        request.accept();
    }

    @Api
    public void party_message(SocialRequest request) {
        engine.message(request.account(), request.message());
        request.accept();
    }
}
