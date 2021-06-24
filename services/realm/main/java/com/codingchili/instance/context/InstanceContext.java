package com.codingchili.instance.context;

import com.codahale.metrics.MetricRegistry;
import com.codingchili.common.ReceivableMessage;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.events.*;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.model.stats.Stats;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;
import com.codingchili.instance.transport.FasterRealmInstanceCodec;
import com.codingchili.realm.configuration.*;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;

import java.util.Random;
import java.util.function.Consumer;

import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.metrics.MetricCollector;

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
    private static final int TIMEOUT_SECONDS = 30_000;
    private final MetricCollector metrics;
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
        this.settings = instance.getPath();
        this.metrics = context.metrics(address());
        this.metrics.settings().setEnabled(
                Configurations.system().getMetrics().isEnabled()
        );

        metrics.type("instance")
                .metadata()
                .put("address", address())
                .put("realm", realm().getId())
                .put("instance", settings().getId());

        this.logger = context.logger(getClass())
                .setMetadataValue(ID_INSTANCE, instance::getId);
    }

    /**
     * @return the unique listening address of this instance.
     */
    public String address() {
        return context.realm().getId() + "." + settings().getId();
    }

    /**
     * @return metrics reporting for the instance.
     */
    public MetricRegistry registry() {
        return metrics.registry();
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
        SpawnPoint point = getSpawnPoint(player);
        Scripted onPlayerJoin = settings().getOnPlayerJoin();

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
            if (player.isFromAnotherInstance() || player.isDead()) {
                player.getVector()
                        .setX(point.getX())
                        .setY(point.getY());
            }
        }
        revive(player);
        player.setFromAnotherInstance(false);
    }

    private void revive(PlayerCreature player) {
        if (player.isDead()) {
            Stats stats = player.getBaseStats();
            Stats computed = player.getStats();
            stats.set(Attribute.health, computed.get(Attribute.maxhealth) * 0.64);
            stats.set(Attribute.energy, computed.get(Attribute.maxenergy) * 0.76);
        }
    }

    private SpawnPoint getSpawnPoint(PlayerCreature player) {
        InstanceSettings settings = settings();
        SpawnPoint point;

        if (settings.getSpawns().isEmpty()) {
            point = new SpawnPoint();
            point.setX((int) player.getVector().getX());
            point.setY((int) player.getVector().getY());

            logger.event("player.spawn", Level.WARNING)
                    .put(ID_INSTANCE, settings.getId())
                    .send("missing spawn point configurations, using player coordinates.");
        } else {
            point = settings.getSpawns().get(
                    new Random().nextInt(settings.getSpawns().size()));
        }
        return point;
    }

    @Override
    public Logger logger(Class aClass) {
        return context.logger(aClass)
                .setMetadataValue(ID_INSTANCE, settings()::getId);
    }

    public void onInstanceStarted(String realm, String instance) {
        logger.event(LOG_INSTANCE_START)
                .put(LOG_INSTANCE, instance)
                .put(ID_REALM, realm).send();
    }

    public void onInstanceStopped(Promise<Void> promise, String realm, String instance) {
        logger.event(LOG_INSTANCE_STOP)
                .put(LOG_INSTANCE, instance)
                .put(ID_REALM, realm).send();

        promise.complete();
    }

    public void skippedTicks(int ticks) {
        logger.event(LOG_INSTANCE_SKIPTICKS, Level.WARNING)
                .put(COUNT, ticks);
    }

    public void onPlayerJoin(JoinMessage join) {
        logger.event(PLAYER_JOIN)
                .put(ID_CHARACTER, join.getPlayer().getName())
                .put(ID_ACCOUNT, join.getPlayer().getAccount())
                .send();
    }

    public void onPlayerLeave(LeaveMessage leave) {
        logger.event(PLAYER_LEAVE)
                .put(ID_CHARACTER, leave.getPlayerName())
                .put(ID_ACCOUNT, leave.getAccountName())
                .send();
    }

    private final DeliveryOptions options = new DeliveryOptions()
            .setSendTimeout(TIMEOUT_SECONDS)
            .setCodecName(FasterRealmInstanceCodec.getName());

    public Future<Object> sendRealm(ReceivableMessage message) {
        Promise<Object> promise = Promise.promise();
        context.bus().request(realm().getId(), message, options, (send) -> {
            if (send.succeeded()) {
                promise.complete(send.result().body());
            } else {
                promise.fail(send.cause());
            }
        });
        return promise.future();
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
