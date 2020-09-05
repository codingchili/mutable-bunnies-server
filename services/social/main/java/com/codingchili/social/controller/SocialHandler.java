package com.codingchili.social.controller;

import com.codingchili.common.Strings;
import com.codingchili.social.configuration.SocialContext;
import io.vertx.core.Future;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.*;

import static com.codingchili.common.Strings.SOCIAL_NODE;

/**
 * @author Robin Duda
 * <p>
 * Social handler to handle friendlists and xr-messaging.
 */
@Address(SOCIAL_NODE)
public class SocialHandler implements CoreHandler {
    private final Protocol<Request> protocol = new Protocol<>(this);
    private SocialContext context;

    public SocialHandler(SocialContext context) {
        this.context = context;
        protocol.authenticator((request) -> context.verify(request.token()).map(v -> Role.USER));
    }

    @Override
    public void init(CoreContext core) {
        protocol.use(Strings.ID_PING, Request::accept, Role.PUBLIC);
        protocol.annotated(new FriendHandler(context));
        protocol.annotated(new PartyHandler(context));
        protocol.annotated(new MessageHandler(context));
    }

    @Override
    public void handle(Request request) {
        protocol.process(new SocialRequest(request));
    }
}