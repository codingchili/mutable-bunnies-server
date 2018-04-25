package com.codingchili.website.configuration;

import io.vertx.core.http.HttpServerRequest;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;

import static com.codingchili.common.Strings.*;
import static com.codingchili.website.configuration.WebserverSettings.PATH_WEBSERVER;

/**
 * @author Robin Duda
 * <p>
 * Context for the web server.
 */
public class WebserverContext extends SystemContext implements ServiceContext {
    private Logger logger;

    public WebserverContext(CoreContext core) {
        super(core);
        this.logger = logger(getClass());
    }

    public WebserverSettings service() {
        return Configurations.get(PATH_WEBSERVER, WebserverSettings.class);
    }

    public void onPageLoaded(HttpServerRequest request) {
        logger.event(LOG_PAGE_LOAD)
                .put(LOG_HOST, request.connection().remoteAddress().host())
                .put(LOG_AGENT, request.getHeader("User-Agent"))
                .send();
    }
}
