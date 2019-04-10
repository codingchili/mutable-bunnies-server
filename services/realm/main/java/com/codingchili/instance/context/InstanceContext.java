package com.codingchili.instance.context;

import com.codingchili.instance.model.SpawnPoint;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.events.*;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;
import com.codingchili.instance.transport.FasterRealmInstanceCodec;
import com.codingchili.instance.transport.ReceivableMessage;
import com.codingchili.realm.configuration.*;
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
    private static final int TIMEOUT_SECONDS = 5000;
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
     * @param player the player creature to SPAWN.
     */
    public void onPlayerJoin(PlayerCreature player) {
        InstanceSettings settings = settings();
        SpawnPoint point;

        if (settings.getSpawns().isEmpty()) {
            point = new SpawnPoint()
                    .setX((int) player.getVector().getX())
                    .setY((int) player.getVector().getY());

            logger.event("player.spawn", Level.WARNING)
                    .put(ID_INSTANCE, settings.getName())
                    .send("missing spawn point configurations, using player coordinates.");
        } else {
            point = settings.getSpawns().get(
                    new Random().nextInt(settings.getSpawns().size()));
        }

        Scripted onPlayerJoin = settings.getOnPlayerJoin();

        if (onPlayerJoin != null) {
            Bindings bindings = new Bindings()
                    .setSource(player)
                    .set("spawn", point)
                    .set("settings", settings)
                    .set("log", (Consumer<Object>) (object) -> {
                        context.logger(getClass()).log(object.toString());
                    });

            onPlayerJoin.apply(bindings);
        } else {
            if (player.isFromAnotherInstance()) {
                player.getVector()
                        .setX(point.getX())
                        .setY(point.getY());
            }
        }
        player.setFromAnotherInstance(false);
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

    private DeliveryOptions options = new DeliveryOptions()
            .setSendTimeout(TIMEOUT_SECONDS)
            .setCodecName(FasterRealmInstanceCodec.getName());

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

    /**
     * Saves the given creature to the character database if it is a player.
     *
     * @param player the player to be saved.
     * @return fluent.
     */
    public InstanceContext save(Creature player) {
        if (player instanceof PlayerCreature) {
            sendRealm(new SavePlayerMessage((PlayerCreature) player));
        }
        return this;
    }
}
