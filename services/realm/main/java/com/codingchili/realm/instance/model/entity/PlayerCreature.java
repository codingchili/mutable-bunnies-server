package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.events.Event;
import com.codingchili.realm.instance.model.events.EventType;
import com.codingchili.realm.instance.model.stats.Attribute;
import com.codingchili.realm.instance.transport.UpdateMessage;

import java.util.Optional;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * model for player characters.
 */
public class PlayerCreature extends SimpleCreature {
    private transient boolean fromAnotherInstance = false;
    private Integer logins = 0;
    private String instance;
    private String className;
    private String account;

    public PlayerCreature() {
    }

    public PlayerCreature(String id) {
        this();
        this.name = id;
    }

    /**
     * @param is true when the character is joining from another instance, in this case then
     *           the existing SPAWN points must be considered. Otherwise the player will retain
     *           their current vector in the world.
     * @return fluent.
     */
    public PlayerCreature setFromAnotherInstance(boolean is) {
        this.fromAnotherInstance = is;
        return this;
    }

    public boolean isFromAnotherInstance() {
        return fromAnotherInstance;
    }

    @Override
    public String getId() {
        return name;
    }

    public Integer getLogins() {
        return logins;
    }

    public void setLogins(Integer logins) {
        this.logins = logins;
    }

    @Override
    public void setContext(GameContext game) {
        Optional<PlayableClass> theClass = game.classes().getByName(className);

        if (theClass.isPresent()) {
            stats = theClass.get().getStats();

            // todo: we must persist SOME attributes without the CLASS attributes.
            stats.set(Attribute.energy, 50);
            stats.set(Attribute.health, 20);
            stats.set(Attribute.experience, 35);
            stats.set(Attribute.nextlevel, 500);
        } else {
            throw new CoreRuntimeException("Class not available: " + className);
        }

        // learn all enabled spells for the current class for now.
        this.spells.getLearned().addAll(theClass.get().getSpells().stream()
                .filter(game.spells()::exists)
                .collect(Collectors.toList()));

        logins++;
        this.game = game;

        game.getInstance().onPlayerJoin(this);
        protocol.annotated(this);

        for (EventType type : EventType.values()) {
            protocol.use(type.toString(), this::handle);
        }
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
        UpdateMessage update = new UpdateMessage(event, account);
        game.getInstance().sendRealm(update).setHandler(done -> {
            if (done.failed()) {
                game.getLogger(getClass()).onError(done.cause());
            }
        });
    }

    private void onError(String msg) {
        game.getLogger(getClass())
                .event("disconnect")
                .put("account", account)
                .put("character", getName())
                .send("failed to message client: " + msg);
    }
}
