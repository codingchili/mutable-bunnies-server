package com.codingchili.social;

import com.codingchili.social.configuration.SocialContext;
import com.codingchili.social.controller.OnlineHandler;
import com.codingchili.social.controller.SocialHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;

/**
 * @author Robin Duda
 * <p>
 * Service for social features such as party and friend lists.
 */
public class Service implements CoreService {
    private CoreContext core;

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public void start(Promise<Void> start) {
        SocialContext.create(core).onComplete(context -> {

            if (context.succeeded()) {
                core.handler(() -> new SocialHandler(context.result()))
                        .compose((a) -> core.handler(() -> new OnlineHandler(context.result())))
                        .onComplete((done) -> {
                            if (done.succeeded()) {
                                start.complete();
                            } else {
                                start.fail(done.cause());
                            }
                        });
            } else {
                start.fail(context.cause());
            }
        });
    }
}
