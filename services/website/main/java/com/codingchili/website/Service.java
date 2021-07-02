package com.codingchili.website;

import com.codingchili.website.configuration.WebserverContext;
import com.codingchili.website.configuration.WebserverSettings;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;

import static com.codingchili.core.context.FutureHelper.untyped;

/**
 * @author Robin Duda
 * <p>
 * Service for the webserver.
 */
public class Service implements CoreService {
    public static final long KB_256 = 256_000;
    private WebserverContext core;
    private WebserverSettings settings;

    @Override
    public void init(CoreContext core) {
        this.core = new WebserverContext(core);
        this.settings = this.core.service();
    }

    @Override
    public void start(Promise<Void> start) {
        core.blocking((blocking) -> {
            Router router = Router.router(core.vertx());
            router.route().handler(
                    BodyHandler.create()
                            .setBodyLimit(KB_256)
                            .setHandleFileUploads(false)
            );

            router.route("/*").handler(ctx -> {
                if (ctx.request().path().equals("/")) {
                    // serve index page directly and avoid redirect.
                    core.onPageLoaded(ctx.request());
                    ctx.response().sendFile(
                            Paths.get(
                                    settings.getResources(),
                                    settings.getStartPage()).toString()
                    );
                } else {
                    ctx.next();
                }
            });

            router.route("/*").handler(StaticHandler.create()
                    .setCachingEnabled(settings.isCache())
                    .setCacheEntryTimeout(300_000L)
                    .setSendVaryHeader(false)
                    .setIndexPage(settings.getStartPage())
                    //.setHttp2PushMapping(List.of(new Http2PushMapping()))
                    .setWebRoot(settings.getResources()));

            router.route().last().handler(ctx -> {
                if (settings.getMissingPage() != null) {
                    String path = Path.of(settings.getResources(), settings.getMissingPage()).toString();
                    ctx.response().setStatusCode(404).sendFile(path);
                } else {
                    ctx.next();
                }
            });

            HttpServerOptions options = settings.getListener().getHttpOptions()
                    .setCompressionSupported(settings.getGzip());

            core.vertx().createHttpServer(options)
                    .requestHandler(router)
                    // logs a lot of connection reset errors with browser clients.
                    .exceptionHandler(e -> { })
                    .listen(settings.getListener().getPort(), untyped(blocking));
        }, start);
    }
}
