package com.codingchili.social.controller;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 */
public interface SocialServiceHandler extends Receiver<Request> {

    @Override
    default void handle(Request request) {
        // not applicable for sub handlers.
    }
}
