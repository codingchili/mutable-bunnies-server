package com.codingchili.realmregistry;

import com.codingchili.common.RegisteredRealm;
import com.codingchili.realmregistry.configuration.RealmRegistrySettings;
import com.codingchili.realmregistry.configuration.RegistryContext;
import com.codingchili.realmregistry.model.AsyncRealmStore;
import com.codingchili.realmregistry.model.RealmDB;
import io.vertx.core.*;

import com.codingchili.core.context.*;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.PrivateMap;
import com.codingchili.core.storage.StorageLoader;

/**
 * @author Robin Duda
 */
public class ContextMock extends RegistryContext {

    public ContextMock() {
        this(new SystemContext());
    }

    public ContextMock(CoreContext context) {
        super(context);
        this.realmFactory = new TokenFactory(context, service().getRealmSecret());
    }

    @Override
    public RealmRegistrySettings service() {
        return new RealmRegistrySettings()
                .setRealmSecret("realms.secret".getBytes())
                .setClientSecret("client.secret".getBytes());
    }

    @Override
    public void getRealmStore(Handler<AsyncResult<AsyncRealmStore>> handler) {
        new StorageLoader<RegisteredRealm>(new StorageContext<>(this))
                .withPlugin(PrivateMap.class)
                .withValue(RegisteredRealm.class)
                .withDB("", "")
                .build(result -> {
                    this.realmDB = new RealmDB(result.result());


                    TokenFactory factory = new TokenFactory(this, "s".getBytes());
                    Token token = new Token("realmName");

                    factory.hmac(token).onComplete(hmac -> {
                        RegisteredRealm realm = new RegisteredRealm()
                                .setId("realmName")
                                .setAuthentication(token);

                        realmDB.put(Promise.promise(), realm);
                        handler.handle(Future.succeededFuture(realmDB));
                    });
                });
    }
}
