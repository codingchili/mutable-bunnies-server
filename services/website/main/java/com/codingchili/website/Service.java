package com.codingchili.website;

import com.codingchili.website.configuration.WebserverContext;
import com.codingchili.website.configuration.WebserverSettings;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;

import static com.codingchili.core.context.FutureHelper.untyped;

/**
 * @author Robin Duda
 * <p>
 * Service for the webserver.
 */
public class Service implements CoreService {
    private WebserverContext core;
    private WebserverSettings settings;

    @Override
    public void init(CoreContext core) {
        this.core = new WebserverContext(core);
        this.settings = this.core.service();
    }

    @Override
    public void start(Future<Void> start) {
        Router router = Router.router(core.vertx());
        router.route().handler(BodyHandler.create());

        router.route("/*").handler(ctx -> {
            if (ctx.request().path().equals("/")) {
                core.onPageLoaded(ctx.request());
            }
            ctx.next();
        });

        router.route("/*").handler(StaticHandler.create()
                .setCachingEnabled(settings.isCache())
                .setCacheEntryTimeout(32000L)
                .setSendVaryHeader(false)
                .setIndexPage(settings.getStartPage())
                .setWebRoot(settings.getResources()));

        HttpServerOptions options = settings.getListener().getHttpOptions(core)
                .setCompressionSupported(settings.getGzip());

        core.vertx().createHttpServer(options)
                .requestHandler(router::accept)
                .listen(settings.getListener().getPort(), untyped(start));
    }
}
