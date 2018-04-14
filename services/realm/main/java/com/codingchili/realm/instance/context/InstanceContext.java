package com.codingchili.realm.instance.context;

import com.codingchili.realm.configuration.*;
import com.codingchili.realm.instance.model.events.JoinMessage;
import com.codingchili.realm.instance.model.events.LeaveMessage;
import io.vertx.core.Future;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 */
public class InstanceContext extends SystemContext implements ServiceContext {
    private static final String LOG_INSTANCE_SKIPTICKS = "skippedTicks";
    private static final String COUNT = "count";
    private static final String PLAYER_JOIN = "player.join";
    private static final String PLAYER_LEAVE = "player.leave";
    private Logger logger;
    private final String settings;
    private final RealmContext context;

    public InstanceContext(RealmContext context, InstanceSettings instance) {
        super(context);
        this.context = context;
        this.logger = context.logger(getClass())
            .setMetadata("instance", instance::getName);
        this.settings = instance.getPath();
    }

    public String address() {
        return context.realm().getName() + "." + settings().getName();
    }

    public InstanceSettings settings() {
        return Configurations.get(settings, InstanceSettings.class);
    }

    public RealmSettings realm() {
        return context.realm();
    }

    public RealmServerSettings service() {
        return context.service();
    }

    @Override
    public Logger logger(Class aClass) {
        return logger;
    }

    public boolean verifyToken(Token token) {
        return context.verifyToken(token);
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
}
