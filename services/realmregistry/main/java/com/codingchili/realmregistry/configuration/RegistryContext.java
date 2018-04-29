package com.codingchili.realmregistry.configuration;

import com.codingchili.common.RegisteredRealm;
import com.codingchili.realmregistry.model.AsyncRealmStore;
import com.codingchili.realmregistry.model.RealmDB;
import io.vertx.core.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.SharedMap;
import com.codingchili.core.storage.StorageLoader;

import static com.codingchili.common.Strings.*;
import static com.codingchili.realmregistry.configuration.RealmRegistrySettings.PATH_REALMREGISTRY;

/**
 * @author Robin Duda
 */
public class RegistryContext extends SystemContext implements ServiceContext {
    protected TokenFactory realmFactory;
    protected TokenFactory clientFactory;
    protected AsyncRealmStore realmDB;
    private AtomicBoolean loading = new AtomicBoolean(false);
    private Queue<Handler<AsyncResult<AsyncRealmStore>>> waiting = new ConcurrentLinkedQueue<>();
    private Logger logger;

    public RegistryContext(CoreContext core) {
        super(core);

        this.realmFactory = new TokenFactory(service().getRealmSecret());
        this.clientFactory = new TokenFactory(service().getClientSecret());
        this.logger = core.logger(getClass());
    }

    public void getRealmStore(Handler<AsyncResult<AsyncRealmStore>> handler) {
        if (realmDB != null) {
            handler.handle(Future.succeededFuture(realmDB));
        } else if (!loading.getAndSet(true)) {
            waiting.add(handler);
            new StorageLoader<RegisteredRealm>(this)
                    .withPlugin(SharedMap.class)
                    .withCollection(COLLECTION_REALMS)
                    .withValue(RegisteredRealm.class)
                    .build(prepare -> {
                        if (prepare.succeeded()) {
                            this.realmDB = new RealmDB(prepare.result());
                            waiting.forEach(waiting -> waiting.handle(Future.succeededFuture(realmDB)));
                        } else {
                            waiting.forEach(waiting -> waiting.handle(Future.failedFuture(prepare.cause())));
                        }
                    });
        } else {
            waiting.add((store) -> handler.handle(Future.succeededFuture(realmDB)));
        }
    }

    public boolean verifyRealmToken(Token token) {
        return realmFactory.verifyToken(token);
    }

    public boolean verifyClientToken(Token token) {
        return clientFactory.verifyToken(token);
    }

    public RealmRegistrySettings service() {
        return Configurations.get(PATH_REALMREGISTRY, RealmRegistrySettings.class);
    }

    public Boolean isTrustedRealm(String name) {
        return service().isTrustedRealm(name);
    }

    public int realmTimeout() {
        return service().getRealmTimeout();
    }

    public void onRealmDisconnect(String realm) {
        logger.event(LOG_REALM_DISCONNECT, Level.ERROR).put(ID_REALM, realm).send();
    }

    public void onRealmUpdated(String realm, int players) {
        logger.event(LOG_REALM_UPDATE, Level.INFO)
                .put(ID_REALM, realm)
                .put(ID_PLAYERS, players).send();
    }

    public void onStaleClearError(Throwable cause) {
        logger.onError(cause);
    }

    public void onStaleRemoveError(Throwable cause) {
        logger.onError(cause);
    }

    public TokenFactory getRealmFactory() {
        return realmFactory;
    }

    public TokenFactory getClientFactory() {
        return clientFactory;
    }
}