package com.codingchili.realm.configuration;

import com.codingchili.realm.model.AsyncCharacterStore;
import com.codingchili.realm.model.CharacterDB;
import io.vertx.core.Future;

import com.codingchili.core.context.*;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.SharedMap;

/**
 * @author Robin Duda
 * <p>
 * Context mock for realms.
 */
public class ContextMock extends RealmContext {
    private static RealmSettings realmSettings = new RealmSettings();

    public ContextMock() {
        this(new SystemContext());
    }

    public ContextMock(CoreContext context) {
        super(context, () -> realmSettings);
    }

    public static Future<ContextMock> create() {
        Future<ContextMock> future = Future.future();
        ContextMock context = new ContextMock();

        TokenFactory factory = new TokenFactory(context, "s".getBytes());
        Token token = new Token("realmName");

        factory.hmac(token).setHandler(hmac -> {
            realmSettings.setId(token.getDomain());
            realmSettings.setAuthentication(token);
            future.complete(context);
        });


        return future;
    }

    @Override
    public AsyncCharacterStore characters() {
        // the sharedMap is set up synchronously.
        return new CharacterDB(new SharedMap<>(Future.future(), new StorageContext<>(this)));
    }

    public TokenFactory getClientFactory() {
        return new TokenFactory(this, realm().getTokenBytes());
    }

    @Override
    public RealmSettings realm() {
        return super.realm();
    }
}
