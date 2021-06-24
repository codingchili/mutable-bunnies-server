package com.codingchili.realmregistry.controller;

import com.codingchili.common.RegisteredRealm;
import com.codingchili.realmregistry.ContextMock;
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
 * tests the API from realmName->authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class RealmRegistryHandlerTest {
    private static final String REALM_NAME = "test-realm";
    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
    private RegisteredRealm realmconfig = new RegisteredRealm();
    private RealmRegistryHandler handler;
    private JsonObject realmToken;
    private ContextMock mock;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        mock = new ContextMock();
        handler = new RealmRegistryHandler(mock);

        Token token = new Token(REALM_NAME);
        mock.getRealmFactory().hmac(token).onComplete(hmac -> {
            realmconfig.setAuthentication(token);
            realmconfig.setId(REALM_NAME);
            realmToken = Serializer.json(token);

            Promise<Void> promise = Promise.promise();
            handler.start(promise);
            promise.future().onComplete(done -> async.complete());
        });
    }

    @After
    public void tearDown(TestContext test) {
        mock.close(test.asyncAssertSuccess());
    }

    @Test
    public void failRegisterRealmTest(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void failWithClientToken(TestContext test) {
        Async async = test.async();
        Token token = new Token(realmconfig.getId());
        mock.getClientFactory().hmac(token).onComplete(hmac -> {

            handle(REALM_UPDATE, (response, status) -> {
                test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
                async.complete();
            }, new JsonObject()
                .put(ID_TOKEN, Serializer.json(token)));
        });
    }

    @Test
    public void updateRealmTest(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put(ID_REALM, Serializer.json(realmconfig))
                .put(ID_TOKEN, realmToken));
    }

    @Test
    public void failUpdateRealmTest(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void testClientClose(TestContext test) {
        // need to register realm before removing
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put(ID_REALM, Serializer.json(realmconfig))
                .put(ID_TOKEN, realmToken));
    }

    @Test
    public void failClientCloseMissingRealm(TestContext test) {
        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, realmToken));
    }

    @Test
    public void failRealmClose(TestContext test) {
        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void testPingAuthenticationHandler(TestContext test) {
        handle(ID_PING, (response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
        });
    }

    @Test
    public void failUpdateWhenInvalidToken(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    @Test
    public void failCloseWhenInvalidToken(TestContext test) {
        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, new JsonObject());
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(RequestMock.get(action, listener, data));
    }
}
