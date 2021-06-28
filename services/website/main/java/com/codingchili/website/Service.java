package com.codingchili.website;

import com.codingchili.website.configuration.WebserverContext;
import com.codingchili.website.configuration.WebserverSettings;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.nio.file.Path;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.logging.Logger;

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
    private Logger logger;

    @Override
    public void init(CoreContext core) {
        this.logger = core.logger(getClass());
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
                            .setDeleteUploadedFilesOnEnd(true)
            );

            router.route("/*").handler(ctx -> {
                if (ctx.request().path().equals("/")) {
                    core.onPageLoaded(ctx.request());
                }
                ctx.next();
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
                    .exceptionHandler(logger::onError)
                    .listen(settings.getListener().getPort(), untyped(blocking));
        }, start);
    }
}
