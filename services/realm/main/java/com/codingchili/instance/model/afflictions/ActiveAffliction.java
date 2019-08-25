package com.codingchili.instance.model.afflictions;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.Ticker;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.model.stats.Stats;
import com.codingchili.instance.scripting.Bindings;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.codingchili.core.logging.Level;

import static com.codingchili.core.configuration.CoreStrings.ID_NAME;

/**
 * Maps an active affliction onto an entity.
 */
public class ActiveAffliction {
    private transient Bindings bindings;
    private Map<String, Object> state = new HashMap<>(); // used by scripts to store data.
    private transient Stats stats = new Stats();
    private String afflictionId;
    private String sourceId;
    private String targetId;
    private int ticks;
    private int interval;
    private int delta;

    // cannot be persisted.
    private transient Affliction affliction;
    private transient GameContext game;
    private transient Creature source;
    private transient Creature target;

    public ActiveAffliction() {
    }

    /**
     * Required call after serialization to initialize any old afflictions.
     *
     * @param game the current game context to retrieve metadata from.
     */
    public void init(GameContext game) {
        this.game = game;
        game.spells().afflictions().getById(afflictionId).ifPresent(affliction -> {
            this.affliction = affliction;
        });
    }

    /**
     * @param source     the source of the affliction.
     * @param target     the target of the affliction.
     * @param affliction the affliction that was created by source on target.
     */
    public ActiveAffliction(Creature source, Creature target, Affliction affliction) {
        this.affliction = affliction;
        this.interval = GameContext.secondsToMs(affliction.getInterval());

        // grant first tick immediately.
        this.ticks = GameContext.secondsToMs(affliction.getDuration()) + interval;
        this.delta = interval;

        this.sourceId = source.getId();
        this.targetId = target.getId();
        this.afflictionId = affliction.getId();

        this.source = source;
        this.target = target;
    }

    /**
     * Modifies the stats on the target creature.
     *
     * @param game the game context.
     * @return the modified stats.
     */
    public Stats modify(GameContext game) {
        stats.clear();
        affliction.apply(getBindings(game).setStats(stats));
        return stats;
    }

    /**
     * @param context the game context that the target exists within.
     * @return true if still active.
     */
    public boolean tick(GameContext context) {
        try {
            while (this.delta > interval) {
                affliction.tick(getBindings(context));
                ticks -= interval;
                this.delta -= interval;
            }
            return (ticks > 0);
        } catch (Exception e) {
            context.getLogger(getClass()).event("affliction.fail", Level.ERROR)
                    .put(ID_NAME, affliction.getName())
                    .send(e.getMessage());
            return false;
        }
    }


    public boolean shouldTick(Ticker ticker) {
        return ((this.delta += ticker.deltaMS()) >= interval);
    }

    private Bindings getBindings(GameContext game) {
        if (bindings == null) {
            bindings = new Bindings()
                    .setContext(game)
                    .setState(state)
                    .set("spells", game.spells())
                    .set("source", source())
                    .set("target", target())
                    .set("log", (Consumer<Object>) (message) -> {
                        game.getLogger(getClass()).event("affliction", Level.INFO)
                                .put("name", affliction.getName())
                                .send(message.toString());
                    })
                    .setAttribute(Attribute.class);
        }
        return bindings;
    }

    private Creature target() {
        return (target != null) ? target : (Creature) game.getById(targetId);
    }

    private Creature source() {
        return (source != null) ? source : (Creature) game.getById(sourceId);
    }

    public String getAfflictionId() {
        return afflictionId;
    }

    public void setAfflictionId(String afflictionId) {
        this.afflictionId = afflictionId;
    }

    public float getDuration() {
        return GameContext.msToSeconds(ticks);
    }

    public Affliction getAffliction() {
        return affliction;
    }

    public void setAffliction(Affliction affliction) {
        this.affliction = affliction;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetId() {
        return targetId;
    }

    public Map<String, Object> getState() {
        return state;
    }

    public void setState(Map<String, Object> state) {
        this.state = state;
    }
}
