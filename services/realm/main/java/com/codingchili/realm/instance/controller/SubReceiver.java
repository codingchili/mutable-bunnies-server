package com.codingchili.realm.instance.controller;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 *
 * Used for nested receivers - the handle method will not be called as they
 * are not mounted on a listener directly. The root handler will invoke this receiver
 * from its own handle method.
 */
public interface SubReceiver extends Receiver<Request> {

    @Override
    default void handle(Request request) {
        // sub-receivers will be handled by the parent handler.
    }
}
