package com.codingchili.realm.configuration;

import com.codingchili.realm.controller.RealmRequest;
import com.codingchili.realm.instance.context.InstanceSettings;
import com.codingchili.realm.instance.model.afflictions.AfflictionDB;
import com.codingchili.realm.instance.model.entity.PlayerCreature;
import com.codingchili.realm.instance.model.spells.SpellDB;
import com.codingchili.realm.instance.scripting.Bindings;
import com.codingchili.realm.instance.scripting.Scripted;
import com.codingchili.realm.model.*;
import io.vertx.core.Future;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.transport.Connection;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.logging.Level.ERROR;
import static com.codingchili.realm.configuration.RealmServerSettings.PATH_REALMSERVER;

/**
 * @author Robin Duda
 * <p>
 * Context for realms.
 */
public class RealmContext extends SystemContext implements ServiceContext {
    private Map<String, Connection> connections = new ConcurrentHashMap<>();
    private AsyncCharacterStore characters;
    private Supplier<RealmSettings> settings;
    private AfflictionDB afflictions;
    private SpellDB spells;
    private ClassDB classes;
    private Logger logger;

    /**
     * @param core     the core context to wrap.
     * @param settings settings for the realm.
     */
    public RealmContext(CoreContext core, Supplier<RealmSettings> settings) {
        super(core);
        this.afflictions = new AfflictionDB(core);
        this.spells = new SpellDB(core);
        this.classes = new ClassDB(core);
        this.settings = settings;
        this.logger = core.logger(getClass())
                .setMetadata("realm", realm()::getNode);
    }

    /**
     * @param core     the core context to create the storage on.
     * @param settings settings for a realm/
     * @return a callback with a RealmContext.
     */
    public static Future<RealmContext> create(CoreContext core, Supplier<RealmSettings> settings) {
        Future<RealmContext> future = Future.future();

        RealmContext context = new RealmContext(core, settings);

        new StorageLoader<PlayerCreature>(new StorageContext<>(context))
                .withPlugin(context.service().getStorage())
                .withValue(PlayerCreature.class)
                .withCollection(settings.get().getNode() + "." + COLLECTION_CHARACTERS)
                .build(storage -> {
                    if (storage.succeeded()) {
                        context.characters = new CharacterDB(storage.result());
                        future.complete(context);
                    } else {
                        future.fail(storage.cause());
                    }
                });

        return future;
    }

    @Override
    public Logger logger(Class aClass) {
        return super.logger(aClass)
                .setMetadata(ID_REALM, settings.get()::getNode);
    }

    public Map<String, Connection> connections() {
        return connections;
    }

    public RealmSettings realm() {
        return settings.get();
    }

    public AsyncCharacterStore characters() {
        return characters;
    }

    public RealmServerSettings service() {
        return Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);
    }

    public ListenerSettings getListenerSettings() {
        return new ListenerSettings().setPort(realm().getPort());
    }

    public List<InstanceSettings> instances() {
        return realm().getInstances();
    }

    public AfflictionDB afflictions() {
        return afflictions;
    }

    public SpellDB spells() {
        return spells;
    }

    public ClassDB classes() {
        return classes;
    }

    public boolean verifyToken(Token token) {
        return new TokenFactory(realm().getTokenBytes()).verifyToken(token);
    }

    public int updateRate() {
        return service().getRealmUpdates();
    }

    public void onRealmStarted(String realm) {
        logger.event(LOG_REALM_START, Level.STARTUP)
                .put(ID_REALM, realm).send();
    }

    public void onRealmRejected(String realm, String message) {
        logger.event(LOG_REALM_REJECTED, Level.WARNING)
                .put(ID_REALM, realm)
                .put(ID_MESSAGE, message).send();
    }

    public void onRealmStopped(Future<Void> future, String realm) {
        logger.event(LOG_REALM_STOP, ERROR)
                .put(ID_REALM, realm).send();

        Delay.forShutdown(future);
    }

    public void onRealmRegistered(String realm) {
        logger.event(LOG_REALM_REGISTERED)
                .put(ID_REALM, realm)
                .send();
    }

    public void onDeployRealmFailure(String realm) {
        logger.event(LOG_REALM_DEPLOY_ERROR, ERROR)
                .put(LOG_MESSAGE, getDeployFailError(realm))
                .send();
    }

    public void onInstanceFailed(String instance, Throwable cause) {
        logger.event(LOG_INSTANCE_DEPLOY_ERROR, ERROR)
                .put(LOG_MESSAGE, getdeployInstanceError(instance, cause))
                .send();
    }

    public void connect(PlayerCreature creature, Connection connection) {
        connection.setProperty(ID_INSTANCE, realm().getNode() + "." + creature.getInstance());
        connections.put(creature.getAccount(), connection);

        logger.log("Account " + creature.getAccount() + " connecting with character " + creature.getName() + " to " + creature.getInstance());

        connection.onCloseHandler("removeConnection", () -> {
            logger.log("Account " + creature.getAccount() + " disconnected with character " + creature.getName());
            connections.remove(creature.getAccount());
        });
    }

    public boolean isConnected(String account) {
        Connection connection = connections.get(account);
        return connection != null && connection.getProperty(ID_INSTANCE).isPresent();
    }

    public void remove(RealmRequest request) {
        connections.remove(request.account());
    }

    public void onPlayerJoin(PlayerCreature creature) {
        Scripted scripted = settings.get().getOnPlayerJoin();
        Bindings bindings = new Bindings();
        bindings.setSource(creature);
        bindings.put("log", (Consumer<Object>) (object) -> {
            System.out.println("source is: " + object.toString());
        });
        scripted.apply(bindings);
    }
}
