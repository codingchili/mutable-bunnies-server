package com.codingchili.instance.model.entity;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.model.stats.Stats;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;
import com.codingchili.instance.transport.UpdateMessage;

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
    public Stats getStats() {
        Stats stats = super.getStats();
        if (game != null) {
            game.classes().getByName(className).ifPresent(playableClass -> {
                stats.apply(playableClass.getStats());
            });
        }
        return stats;
    }

    @Override
    public void setContext(GameContext game) {
        this.game = game;

        Optional<PlayableClass> theClass = game.classes().getByName(className);

        if (theClass.isPresent()) {
            Scripted scaling = game.instance().realm().getLevelScaling();
            Bindings bindings = new Bindings();
            bindings.setSource(this);
            bindings.setAttribute(Attribute.class);

            stats.set(Attribute.maxhealth, getStats().get(Attribute.constitution) * 10);
            stats.set(Attribute.maxenergy, getStats().get(Attribute.dexterity) * 20 + 100);
            stats.setDefault(Attribute.health, getStats().get(Attribute.maxhealth));
            stats.setDefault(Attribute.energy, getStats().get(Attribute.maxenergy));
            stats.setDefault(Attribute.experience, 15.0f);
            stats.setDefault(Attribute.level, 1);

            stats.set(Attribute.nextlevel, scaling.apply(bindings));
        } else {
            throw new CoreRuntimeException("Class not available: " + className);
        }

        // learn all enabled spells for the current class for now.
        this.spells.getLearned().addAll(theClass.get().getSpells().stream()
                .filter(game.spells()::exists)
                .collect(Collectors.toList()));

        logins++;
        this.game = game;

        game.instance().onPlayerJoin(this);
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
        game.instance().sendRealm(update).setHandler(done -> {
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
