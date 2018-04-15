package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.events.Event;
import com.codingchili.realm.instance.model.events.EventType;
import com.codingchili.realm.instance.transport.PlayerRequest;
import io.vertx.core.Future;

import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.CoreStrings.throwableToString;

/**
 * @author Robin Duda
 * <p>
 * model for player characters.
 */
public class PlayerCreature extends SimpleCreature {
    private String instance = "level 1";
    private String className;
    private String account;
    private String realmName;

    public PlayerCreature() {
    }

    public PlayerCreature(String id) {
        this.name = id;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public void setContext(GameContext context) {
        this.context = context;
        this.realmName = context.getInstance().realm().getName();
        protocol.annotated(this);
        for (EventType type : EventType.values()) {
            protocol.use(type.toString(), this::handle);
        }
        context.subscribe(this);
    }

    public String getAccount() {
        return account;
    }

    public PlayerCreature setAccount(String account) {
        this.account = account;
        return this;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public void handle(Event event) {
        Future<Object> future = Future.future();
        PlayerRequest message = new PlayerRequest(future, event, account);
        future.setHandler((reply) -> {
            if (reply.failed()) {
                onError(throwableToString(reply.cause()));
            }
        });
        context.getInstance().bus().send(realmName, message);
    }

    private void onError(String msg) {
        context.getLogger(getClass())
                .event("disconnect")
                .put("account", account)
                .put("character", getName())
                .send("failed to message client: " + msg);
    }

    public static void main(String[] args) {
        System.out.println(Serializer.pack(new PlayerCreature()));
    }
}
