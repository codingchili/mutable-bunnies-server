package com.codingchili.instance.model.entity;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.Ticker;
import com.codingchili.instance.model.afflictions.ActiveAffliction;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;
import com.codingchili.instance.model.events.NotificationEvent;
import com.codingchili.instance.model.questing.QuestState;
import com.codingchili.instance.model.skills.SkillState;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.model.stats.Stats;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;
import com.codingchili.instance.transport.UpdateMessage;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Robin Duda
 * <p>
 * model for player characters.
 */
public class PlayerCreature extends SimpleCreature {
    private transient boolean fromAnotherInstance = false;
    private transient Ticker healthRegeneration;
    private transient Ticker energyRegeneration;
    private QuestState quests = new QuestState();
    private Integer logins = 0;
    private String instance;
    private String classId;
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
    protected boolean onClassModifier(Stats calculated) {
        if (game != null) {
            game.classes().getById(classId).ifPresent(playableClass -> {
                calculated.apply(playableClass.getStats());
            });
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setContext(GameContext game) {
        this.game = game;

        Optional<PlayableClass> theClass = game.classes().getById(classId);

        if (theClass.isPresent()) {
            Scripted scaling = game.instance().realm().getLevelScaling();
            Bindings bindings = new Bindings()
                    .setSource(this)
                    .setAttribute(Attribute.class);

            setModel(theClass.get().getModel());

            baseStats.setDefault(Attribute.experience, 15.0f);
            baseStats.set(Attribute.nextlevel, scaling.apply(bindings));

            for (ActiveAffliction affliction : afflictions) {
                affliction.init(game);
            }

            // learn all enabled spells for the current class for now.
            this.spells.getLearned().addAll(theClass.get().getSpells().stream()
                    .filter(game.spells()::exists)
                    .collect(Collectors.toList()));

            game.instance().onPlayerJoin(this);
            protocol.annotated(this);

            for (EventType type : EventType.values()) {
                protocol.use(type.toString(), this::handle);
            }

            logins++;
        } else {
            throw new CoreRuntimeException("Class not available: " + classId);
        }
    }

    @Override
    public void joined() {
        healthRegeneration = game.ticker(ticker -> {
            game.spells().heal(this, getStats().get(Attribute.maxhealth) * 0.01);
        }, GameContext.secondsToTicks(5));

        energyRegeneration = game.ticker(ticker -> {
            game.spells().energy(this, 10);
        }, GameContext.secondsToTicks(1));
    }

    @Override
    public void removed() {
        energyRegeneration.disable();
        healthRegeneration.disable();
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

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public QuestState getQuests() {
        return quests;
    }

    public void setQuests(QuestState quests) {
        this.quests = quests;
    }

    @Override
    public void handle(Event event) {
        UpdateMessage update = new UpdateMessage(event, account);
        game.instance().sendRealm(update).onComplete(done -> {
            if (done.failed()) {
                onError(done.cause().getMessage());
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
