package com.codingchili.realm.configuration;

import com.codingchili.instance.context.InstanceSettings;
import com.codingchili.instance.model.afflictions.AfflictionDB;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.spells.SpellDB;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;
import com.codingchili.instance.transport.FasterRealmInstanceCodec;
import com.codingchili.realm.controller.RealmRequest;
import com.codingchili.realm.model.*;
import io.vertx.core.Future;
import io.vertx.core.eventbus.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.transport.Connection;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.logging.Level.ERROR;
import static com.codingchili.realm.configuration.RealmServerSettings.PATH_REALMSERVER;
import static io.vertx.core.eventbus.ReplyFailure.*;

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

    private DeliveryOptions delivery = new DeliveryOptions()
            .setCodecName(FasterRealmInstanceCodec.getName());

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

        delivery.setSendTimeout(realm().getListener().getTimeout());
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

    public Future<Object> sendInstance(String instance, Object data) {
        Future<Object> future = Future.future();

        bus().send(instance, data, delivery, handler -> {
            // only process missing handlers and recipient failures - timeouts are expected.
            if (handler.failed() && handler.cause() instanceof ReplyException) {
                ReplyFailure type = ((ReplyException) handler.cause()).failureType();
                if (type == NO_HANDLERS || type == RECIPIENT_FAILURE) {
                    logger.onError(handler.cause());
                    future.fail(new CoreRuntimeException(throwableToString(handler.cause())));
                }
            }
            // ignore failures - the instance does not need to respond synchronously.
            // if they do respond - make sure the client receives it.
            if (handler.succeeded()) {
                future.complete(handler.result().body());
            }
        });
        return future;
    }

    @Override
    public Logger logger(Class aClass) {
        Logger logger = super.logger(aClass);

        if (settings != null) {
            logger.setMetadata(ID_REALM, settings.get()::getNode);
        }
        return logger;
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

    public Future<Void> verifyToken(Token token) {
        TokenFactory factory = new TokenFactory(this, realm().getTokenBytes());
        return factory.verify(token);
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

        future.complete();
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

    public String setInstance(PlayerCreature creature, Connection connection) {
        String address = realm().getNode() + "." + creature.getInstance();
        connection.setProperty(ID_INSTANCE, address);
        return address;
    }

    public void connect(Connection connection, String account) {
        connections.put(account, connection);

        logger.log("Account " + account + " connected to " + settings.get().getNode());

        connection.onCloseHandler("remove-connection", () -> {
            logger.log("Account " + account + " disconnected from " + settings.get().getNode());
            connections.remove(account);
            notify(account, false);
        });
        notify(account, true);
    }

    private void notify(String account, boolean online) {
        bus().send(ONLINE_SOCIAL_NODE, Serializer.json(
                new OnlineStatusMessage(account, realm().getNode(), online)));
    }

    public void clearInstance(RealmRequest request) {
        request.connection().setProperty(ID_INSTANCE, null);
    }

    public boolean isConnected(String account) {
        return connections.containsKey(account);
    }

    public void remove(String account) {
        connections.remove(account);
    }

    public void onPlayerJoin(PlayerCreature creature) {
        Scripted scripted = settings.get().getOnPlayerJoin();

        if (scripted != null) {
            Bindings bindings = new Bindings();
            bindings.setSource(creature);
            bindings.setAttribute(Attribute.class);
            bindings.put("log", (Consumer<Object>) (object) -> {
                System.out.println("source is: " + object.toString());
            });
            scripted.apply(bindings);
        }
    }
}
