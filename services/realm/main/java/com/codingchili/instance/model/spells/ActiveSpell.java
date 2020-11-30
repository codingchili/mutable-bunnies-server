package com.codingchili.instance.model.spells;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.Ticker;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.scripting.Bindings;

import java.util.Objects;
import java.util.function.Consumer;

import com.codingchili.core.logging.Level;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * <p>
 * A spell that is being casted or has been casted.
 */
public class ActiveSpell {
    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String GAME = "game";
    public static final String ACTIVE = "active";
    public static final String SPELLS = "spells";
    public static final String STAGE = "stage";
    private transient Bindings bindings = null;
    private transient int interval;
    private SpellCycle cycle = SpellCycle.CASTING;
    private SpellTarget target;
    private Creature source;
    private Spell spell;
    private int progress = 0;
    private int timer;
    private float delta = 0f;

    public ActiveSpell(Spell spell) {
        this.progress = GameContext.secondsToMs(spell.getCasttime());
        this.interval = GameContext.secondsToMs(spell.getInterval());
        this.spell = spell;
    }

    public boolean completed(Ticker ticker) {
        this.timer = GameContext.secondsToMs(spell.getActive());
        return ((progress -= ticker.deltaMS()) <= 0);
    }

    public boolean active(Ticker ticker) {
        return ((timer -= ticker.deltaMS()) >= 0);
    }

    public void onCastProgress(GameContext game) {
        if (spell.getOnCastProgress() != null) {
            Bindings bindings = getBindings(game, SpellStage.PROGRESS);
            try {
                while (this.delta > interval) {
                    spell.getOnCastProgress().apply(bindings);
                    this.delta -= interval;
                }
            } catch (Throwable e) {
                game.getLogger(getClass()).onError(e);
            }
        }
    }

    public void onCastCompleted(GameContext game) {
        if (spell.getOnCastComplete() != null) {
            try {
                spell.getOnCastComplete().apply(
                        getBindings(game, SpellStage.COMPLETED)
                );
            } catch (Throwable e) {
                game.getLogger(getClass()).onError(e);
            }
        }
    }

    public boolean onCastBegin(GameContext game) {
        if (spell.getOnCastBegin() != null) {
            Boolean check = spell.getOnCastBegin().apply(
                    getBindings(game, SpellStage.BEGIN)
            );

            Objects.requireNonNull(check,
                    "onCastBegin check of spell " + spell.getId() + " returned 'null'.");

            if (check) {
                // only put on cooldown if precondition check succeeded.
                source.getSpells().setCooldown(spell);
            }
            return check;
        } else {
            return true;
        }
    }

    public void onSpellActive(GameContext game) {
        if (spell.getOnSpellActive() != null) {
            Bindings bindings = getBindings(game, SpellStage.ACTIVE);

            while (this.delta > interval) {
                spell.getOnSpellActive().apply(bindings);
                this.delta -= interval;
            }
        }
    }

    public boolean shouldTick(Ticker ticker) {
        return ((this.delta += ticker.deltaMS()) >= GameContext.secondsToMs(spell.getInterval()));
    }

    private Bindings getBindings(GameContext game, SpellStage stage) {
        return getBindings(game)
                .set(STAGE, stage);
    }

    private Bindings getBindings(GameContext game) {
        if (bindings == null) {
            bindings = new Bindings();
            bindings.put(SOURCE, source);
            bindings.put(TARGET, target);
            bindings.put(SPELLS, game.spells());
            bindings.put(GAME, game);
            bindings.put(ID_LOG, (Consumer<String>) (line) -> {
                game.getLogger(getClass()).event("spell", Level.INFO)
                        .put(ID_NAME, spell.getId())
                        .send();
            });
            bindings.setAttribute(Attribute.class);
            bindings.put(ACTIVE, this);
        }
        return bindings;
    }

    @FunctionalInterface
    private interface DamageWrapper {
        /**
         * Wraps the damage function to automatically provide source and spell name to the
         * spell engine.
         *
         * @param target the target receiving the damage.
         * @param value the amount of damage to deal.
         * @param type the type of damage to deal.
         */
        void damage(Creature target, Float value, ModifierType type);
    }

    public Spell getSpell() {
        return spell;
    }

    public Creature getSource() {
        return source;
    }

    public ActiveSpell setSource(Creature source) {
        this.source = source;
        return this;
    }

    public SpellTarget getTarget() {
        return target;
    }

    public ActiveSpell setTarget(SpellTarget target) {
        this.target = target;
        return this;
    }

    public int getProgress() {
        return progress;
    }

    public ActiveSpell setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public SpellCycle getCycle() {
        return cycle;
    }

    public ActiveSpell setCycle(SpellCycle cycle) {
        this.cycle = cycle;
        return this;
    }
}
