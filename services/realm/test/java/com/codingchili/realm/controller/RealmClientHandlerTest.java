package com.codingchili.realm.controller;

import com.codingchili.common.Strings;
import com.codingchili.realm.configuration.ContextMock;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.realm.model.AsyncCharacterStore;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * tests the API from client->realmName.
 */

@RunWith(VertxUnitRunner.class)
public class RealmClientHandlerTest {
    private static final String USERNAME = "username";
    private static final String CHARACTER_NAME_DELETED = "character-deleted";
    private static final String CHARACTER_NAME = "character";
    private static final String CLASS_NAME = "class.name";
    private static final String ROUTE_CONNECT = "connect";
    private static final String ROUTE_AFFLICTIONS = "afflictioninfo";
    private static final String ROUTE_CLASSES = "classinfo";
    private static final String ROUTE_SPELLS = "spellinfo";

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    private AsyncCharacterStore characters;
    private JsonObject clientToken;
    private RealmClientHandler handler;
    private ContextMock context;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();

        ContextMock.create().onComplete(mock -> {
            context = mock.result();
            handler = new RealmClientHandler(context);

            Token token = new Token(USERNAME);
            context.getClientFactory().hmac(token).onComplete(hmac -> {
                clientToken = Serializer.json(token);
                characters = context.characters();
                createCharacters(async);
                handler.start(Promise.promise());
            });
        });
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    private void createCharacters(Async async) {
        PlayerCreature add = new PlayerCreature(CHARACTER_NAME).setAccount(USERNAME);
        PlayerCreature delete = new PlayerCreature(CHARACTER_NAME_DELETED).setAccount(USERNAME);
        Promise<Void> addPromise = Promise.promise();
        Promise<Void> removePromise = Promise.promise();

        CompositeFuture.all(addPromise.future(), removePromise.future()).onComplete(done -> async.complete());

        characters.create(addPromise, add);
        characters.create(removePromise, delete);
    }

    @Test
    public void realmPingTest(TestContext test) {
        // send a ping request without authentication.
        handler.handle(RequestMock.get(ID_PING, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()));
    }

    @Test
    public void removeCharacter(TestContext test) {
        Async async = test.async();

        handleAuthenticated(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME_DELETED));
    }

    @Test
    public void createCharacter(TestContext test) {
        Async async = test.async();

        handleAuthenticated(CLIENT_CHARACTER_CREATE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME + ".NEW")
                .put(ID_CLASS, CLASS_NAME)
                .put(ID_TOKEN, clientToken));
    }

    @Test
    public void failOverwriteExistingCharacter(TestContext test) {
        Async async = test.async();
        handleAuthenticated(CLIENT_CHARACTER_CREATE, (response, status) -> {
            test.assertEquals(ResponseStatus.CONFLICT, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME)
                .put(ID_CLASS, CLASS_NAME)
                .put(ID_TOKEN, clientToken));
    }

    @Test
    public void failToRemoveMissingCharacter(TestContext test) {
        Async async = test.async();

        handleAuthenticated(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            test.assertEquals(ResponseStatus.ERROR, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME + ".MISSING")
                .put(ID_TOKEN, clientToken));
    }

    @Test
    public void listCharactersOnRealm(TestContext test) {
        Async async = test.async();

        handleAuthenticated(CLIENT_CHARACTER_LIST, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertTrue(characterInJsonArray(response.getJsonArray(ID_CHARACTERS)));
            async.complete();
        }, new JsonObject());
    }

    private boolean characterInJsonArray(JsonArray characters) {
        Boolean found = false;

        for (int i = 0; i < characters.size(); i++) {
            if (characters.getJsonObject(i).getString(ID_NAME).equals(CHARACTER_NAME))
                found = true;
        }
        return found;
    }

    @Test
    public void getRealmData(TestContext test) {
        handleAuthenticated(ROUTE_CONNECT, (response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
            test.assertFalse(response.isEmpty());
        }, new JsonObject());
    }

    @Test
    public void getAfflictionInfo(TestContext test){
        handle(ROUTE_AFFLICTIONS, (response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
            test.assertFalse(response.isEmpty());
        }, new JsonObject());
    }

    @Test
    public void getClassInfo(TestContext test) {
        handle(ROUTE_CLASSES, (response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
            test.assertFalse(response.isEmpty());
        }, new JsonObject());
    }

    @Test
    public void getSpellInfo(TestContext test) {
        handle(ROUTE_SPELLS, (response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
            test.assertFalse(response.isEmpty());
        }, new JsonObject());
    }

    @Test
    public void failListCharactersOnRealmWhenInvalidToken(TestContext test) {
        handler.handle(RequestMock.get(Strings.CLIENT_CHARACTER_LIST, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, Serializer.json(getInvalidClientToken()))));
    }

    @Test
    public void failToCreateCharacterWhenInvalidToken(TestContext test) {
        handler.handle(RequestMock.get(Strings.CLIENT_CHARACTER_CREATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, getInvalidClientToken())));
    }

    @Test
    public void failToRemoveCharacterWhenInvalidToken(TestContext test) {
        handler.handle(RequestMock.get(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, getInvalidClientToken())));
    }

    private void handleAuthenticated(String action, ResponseListener listener, JsonObject data) {
        Request request = RequestMock.get(action, listener, data);
        request.connection().setProperty(ID_ACCOUNT, USERNAME);
        handler.handle(request);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(RequestMock.get(action, listener, data));
    }

    private JsonObject getInvalidClientToken() {
        return Serializer.json(new Token("username").setKey("bogus"));
    }
}
