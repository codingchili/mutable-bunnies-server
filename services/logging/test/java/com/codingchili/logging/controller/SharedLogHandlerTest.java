package com.codingchili.logging.controller;

import com.codingchili.common.Strings;
import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.configuration.LogServerSettings;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.logging.AbstractLogger;
import com.codingchili.core.logging.Level;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Base test cases for log handlers.
 */
@Ignore("Extend this class to run the test cases")
@RunWith(VertxUnitRunner.class)
public class SharedLogHandlerTest {
    private static final int MESSAGE_COUNT = 20;
    @Rule
    public Timeout timeout = new Timeout(50000, TimeUnit.SECONDS);
    CoreHandler handler;
    LogContext context;
    private TokenFactory factory;
    private Promise<Void> promise = Promise.promise();

    public SharedLogHandlerTest() {
        LogServerSettings settings = new LogServerSettings()
                .setStorage(true);

        Configurations.put(settings);
        SystemContext system = new SystemContext();
        settings.setLoggingSecret(new byte[]{0x0});
        settings.setClientSecret(new byte[]{0x0});
        context = new LogContext(system, promise);
        factory = new TokenFactory(context, settings.getLoggingSecret());
    }

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();

        promise.future().onComplete(done -> {
            context.storage().clear(cleared -> {
                async.complete();
            });
        });
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void logMessage(TestContext test) {
        Async async = test.async();

        Token token = new Token("domain");
        factory.hmac(token).onComplete(done -> {
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                handle(Strings.PROTOCOL_LOGGING, (response, status) -> {
                }, messageWithToken(token));
            }
        });
        context.periodic(TimerSource.of(20), event -> {
            context.storage().size(size -> {
                if (size.result() == MESSAGE_COUNT) {
                    context.cancel(event);
                    async.complete();
                }
            });
        });
    }

    void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(RequestMock.get(action, listener, data));
    }

    private JsonObject messageWithToken(Token token) {
        return getLogMessage().put(ID_TOKEN, Serializer.json(token));
    }

    protected JsonObject getLogMessage() {
        return new JsonObject()
                .put(ID_MESSAGE, new LogMessageGenerator(null, getClass())
                        .event("test-event", Level.WARNING).put("unique", UUID.randomUUID().toString()).toJson());
    }

    static class LogMessageGenerator extends AbstractLogger {
        public LogMessageGenerator(CoreContext context, Class aClass) {
            super(context, aClass);
        }

        @Override
        public void close() {
            // no-op.
        }
    }
}
