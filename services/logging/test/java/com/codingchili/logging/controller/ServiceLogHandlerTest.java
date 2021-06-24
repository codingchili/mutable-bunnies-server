package com.codingchili.logging.controller;

import com.codingchili.core.context.SystemContext;
import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.configuration.LogServerSettings;
import io.vertx.core.Promise;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 * <p>
 * Tests the logging server.
 */
@RunWith(VertxUnitRunner.class)
public class ServiceLogHandlerTest extends SharedLogHandlerTest {
    private Promise<Void> promise = Promise.promise();

    public ServiceLogHandlerTest() {
        super();
        handler = new ServiceLogHandler(context);
        LogServerSettings.get().setClientSecret("foo".getBytes());
    }

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        context = new LogContext(new SystemContext(), promise);

        promise.future().onComplete(done -> {
            context.storage().clear(clear -> {
                async.complete();
            });
            handler = new ServiceLogHandler(context);
        });
    }
}