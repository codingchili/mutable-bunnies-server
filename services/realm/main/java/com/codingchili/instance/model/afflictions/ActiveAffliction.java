package com.codingchili.instance.model.afflictions;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.spells.DamageType;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.model.stats.Stats;
import com.codingchili.instance.scripting.Bindings;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private Stats stats = new Stats();
    private Affliction affliction;
    private Creature source;
    private Creature target;
    private Integer ticks;
    private Integer interval;
    private Float delta = 0f;
    private Long start = System.currentTimeMillis();

    /**
     * @param source     the source of the affliction.
     * @param target     the target of the affliction.
     * @param affliction the affliction that was created by source on target.
     */
    public ActiveAffliction(Creature source, Creature target, Affliction affliction) {
        this.source = source;
        this.target = target;
        this.affliction = affliction;
        this.ticks = GameContext.secondsToTicks(affliction.getDuration());
        this.interval = GameContext.secondsToTicks(affliction.getInterval());
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

    @JsonIgnore
    public Long getStart() {
        return start;
    }

    /**
     * @param context the game context that the target exists within.
     * @return true if still active.
     */
    public boolean tick(GameContext context) {
        try {
            do {
                affliction.tick(getBindings(context));
                ticks -= interval;
                this.delta -= interval;
            } while (this.delta >= 1);

            return (ticks > 0);
        } catch (Exception e) {
            context.getLogger(getClass()).event("affliction.fail", Level.ERROR)
                    .put(ID_NAME, affliction.getName())
                    .send(e.getMessage());
            return false;
        }
    }


    public boolean shouldTick(float delta) {
        return ((this.delta += delta) >= interval);
    }

    private Bindings getBindings(GameContext context) {
        if (bindings == null) {
            bindings = new Bindings()
                    .setContext(context)
                    .setState(state)
                    .set("spells", context.spells())
                    .set("source", source)
                    .set("target", target)
                    .set("DamageType", DamageType.class)
                    .set("log", (Consumer<Object>) (message) -> {
                        context.getLogger(getClass()).event("affliction", Level.INFO)
                                .put("name", affliction.getName())
                                .send(message.toString());
                    })
                    .setAttribute(Attribute.class);
        }
        return bindings;
    }

    public Affliction getAffliction() {
        return affliction;
    }

    public void setAffliction(Affliction affliction) {
        this.affliction = affliction;
    }

    public String getSourceId() {
        return source.getId();
    }

    public String getTargetId() {
        return target.getId();
    }

    @JsonIgnore
    public Creature getSource() {
        return source;
    }

    @JsonIgnore
    public Creature getTarget() {
        return target;
    }
}
