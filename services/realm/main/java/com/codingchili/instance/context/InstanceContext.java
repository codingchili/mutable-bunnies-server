package com.codingchili.instance.context;

import com.codingchili.realm.configuration.*;
import com.codingchili.instance.model.SpawnPoint;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.events.JoinMessage;
import com.codingchili.instance.model.events.LeaveMessage;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.transport.FasterRealmInstanceCodec;
import com.codingchili.instance.transport.ReceivableMessage;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;

import java.util.Random;
import java.util.function.Consumer;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * <p>
 * The instance context contains instance specific configuration and utility methods.
 */
public class InstanceContext extends SystemContext implements ServiceContext {
    private static final String LOG_INSTANCE_SKIPTICKS = "skippedTicks";
    private static final String COUNT = "count";
    private static final String PLAYER_JOIN = "player.join";
    private static final String PLAYER_LEAVE = "player.leave";
    private final String settings;
    private final RealmContext context;
    private Logger logger;

    /**
     * Creates a new instance context from the given realm context and instance settings.
     *
     * @param context  the realm context the instance context is attached to.
     * @param instance the instance settings.
     */
    public InstanceContext(RealmContext context, InstanceSettings instance) {
        super(context);
        this.context = context;

        this.logger = context.logger(getClass())
                .setMetadata(ID_INSTANCE, instance::getName);

        this.settings = instance.getPath();
    }

    /**
     * @return the unique listening address of this instance.
     */
    public String address() {
        return context.realm().getNode() + "." + settings().getName();
    }

    /**
     * @return retrieve the latest version of the instance settings.
     */
    public InstanceSettings settings() {
        return Configurations.get(settings, InstanceSettings.class);
    }

    /**
     * @return the latest version of the realm settings.
     */
    public RealmSettings realm() {
        return context.realm();
    }

    /**
     * @return the latest version of the realm server settings.
     */
    public RealmServerSettings service() {
        return context.service();
    }

    /**
     * Executes the onPlayerSpawn script defined in the instance settings.
     *
     * @param playerCreature the player creature to SPAWN.
     */
    public void onPlayerJoin(PlayerCreature playerCreature) {
        InstanceSettings settings = settings();
        SpawnPoint point = settings.getSpawns().get(new Random().nextInt(settings.getSpawns().size()));

        Bindings bindings = new Bindings()
                .setSource(playerCreature)
                .set("spawn", point)
                .set("settings", settings)
                .set("log", (Consumer<Object>) (object) -> {
                    context.logger(getClass()).log(object.toString());
                });
        settings.getOnPlayerJoin().apply(bindings);
    }

    @Override
    public Logger logger(Class aClass) {
        return context.logger(aClass)
                .setMetadata(ID_INSTANCE, settings()::getName);
    }

    public void onInstanceStarted(String realm, String instance) {
        logger.event(LOG_INSTANCE_START, Level.STARTUP)
                .put(LOG_INSTANCE, instance)
                .put(ID_REALM, realm).send();
    }

    public void onInstanceStopped(Future<Void> future, String realm, String instance) {
        logger.event(LOG_INSTANCE_STOP, Level.ERROR)
                .put(LOG_INSTANCE, instance)
                .put(ID_REALM, realm).send();

        Delay.forShutdown(future);
    }

    public void skippedTicks(int ticks) {
        logger.event(LOG_INSTANCE_SKIPTICKS, Level.WARNING)
                .put(COUNT, ticks);
    }

    public void onPlayerJoin(JoinMessage join) {
        logger.event(PLAYER_JOIN, Level.INFO)
                .put(ID_NAME, join.getPlayer().getName())
                .put(ID_ACCOUNT, join.getPlayer().getAccount())
                .send();
    }

    public void onPlayerLeave(LeaveMessage leave) {
        logger.event(PLAYER_LEAVE, Level.INFO)
                .put(ID_NAME, leave.getPlayerName())
                .put(ID_ACCOUNT, leave.getAccountName())
                .send();
    }

    private DeliveryOptions options = new DeliveryOptions().setCodecName(FasterRealmInstanceCodec.getName());
    public Future<Object> sendRealm(ReceivableMessage message) {
        Future<Object> future = Future.future();
        context.bus().send(realm().getNode(), message, options, (send) -> {
            if (send.succeeded()) {
                future.complete(send.result().body());
            } else {
                future.fail(send.cause());
            }
        });
        return future;
    }
}