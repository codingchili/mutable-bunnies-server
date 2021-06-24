package com.codingchili.realmregistry.controller;

import com.codingchili.common.Strings;
import com.codingchili.realmregistry.ContextMock;
import com.codingchili.realmregistry.model.RealmList;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.security.Token;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * tests the API from client -> authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class RealmRegistryClientHandlerTest {
    private static final String USERNAME = "username";
    private static final String REALM_NAME = "realmName";
    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    private RealmRegistryClientHandler handler;
    private ContextMock context;
    private JsonObject clientToken;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();

        Token token = new Token(USERNAME);
        context = new ContextMock();

        context.getClientFactory().hmac(token).onComplete(hmac -> {
            clientToken = Serializer.json(token);
            handler = new RealmRegistryClientHandler(context);
            Promise<Void> promise = Promise.promise();
            handler.start(promise);
            promise.future().onComplete(done -> async.complete());
        });
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void listRealms(TestContext test) {
        Async async = test.async();

        handle(CLIENT_REALM_LIST, (response, status) -> {
            RealmList realms = Serializer.unpack(response, RealmList.class);

            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertNotNull(realms);
            test.assertFalse(realms.getRealms().isEmpty());

            async.complete();
        });
    }

    @Test
    public void createRealmToken(TestContext test) {
        Async async = test.async();

        handle(CLIENT_REALM_TOKEN, (response, status) -> {
            Token token = Serializer.unpack(response.getJsonObject(ID_TOKEN), Token.class);

            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertEquals(USERNAME, token.getDomain());

            async.complete();
        }, new JsonObject()
                .put(ID_TOKEN, clientToken)
                .put(ID_REALM, REALM_NAME));
    }

    @Test
    public void failCreateRealmTokenWhenInvalidToken(TestContext test) {
        handle(Strings.CLIENT_REALM_TOKEN, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, getInvalidClientToken()));
    }

    @Test
    public void testPingClientHandler(TestContext test) {
        handle(ID_PING, ((response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
        }));
    }

    private JsonObject getInvalidClientToken() {
        return Serializer.json(new Token("hey").setKey("xxx"));
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, new JsonObject().put(ID_TOKEN, clientToken));
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(RequestMock.get(action, listener, data));
    }
}
