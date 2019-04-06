package com.codingchili.instance.model.spells;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.events.SpellCycle;
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
    private static final String TICK = "tick";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String GAME = "game";
    private static final String ACTIVE = "active";
    private static final String SPELLS = "spells";
    private static final String DAMAGE_TYPE = "DamageType";
    private transient Bindings bindings = null;
    private SpellCycle cycle = SpellCycle.CASTING;
    private SpellTarget target;
    private Creature source;
    private Spell spell;
    private int progress = 0;
    private int timer;
    private float delta = 0f;

    public ActiveSpell(Spell spell) {
        this.progress = GameContext.secondsToTicks(spell.getCasttime());
        this.spell = spell;
    }

    public boolean completed(float delta) {
        this.timer = GameContext.secondsToTicks(spell.getActive());
        return ((progress -= delta) <= 0);
    }

    public boolean active(float delta) {
        return ((timer -= delta) >= 0);
    }

    public void onCastProgress(GameContext game) {
        if (spell.getOnCastProgress() != null) {
            Bindings bindings = getBindings(game);
            try {
                do {
                    spell.getOnCastProgress().apply(bindings);
                    this.delta -= GameContext.secondsToTicks(spell.getInterval());
                } while (this.delta > 0);
            } catch (Throwable e) {
                game.getLogger(getClass()).onError(e);
            }
        }
    }

    public void onCastCompleted(GameContext game) {
        if (spell.getOnCastComplete() != null) {
            try {
                spell.getOnCastComplete().apply(getBindings(game));
            } catch (Throwable e) {
                game.getLogger(getClass()).onError(e);
            }
        }
    }

    public boolean onCastBegin(GameContext game) {
        if (spell.getOnCastBegin() != null) {
            Boolean check = spell.getOnCastBegin().apply(getBindings(game));

            Objects.requireNonNull(check,
                    "onCastBegin check of spell " + spell.getInterval() + " returned 'null'.");

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
            Bindings bindings = getBindings(game);
            do {
                spell.getOnSpellActive().apply(bindings);
                this.delta -= GameContext.secondsToTicks(spell.getInterval());
            } while (this.delta > 0);
        }
    }

    public boolean shouldTick(float delta) {
        return ((this.delta += delta) >= spell.getInterval());
    }

    private Bindings getBindings(GameContext game) {
        if (bindings == null) {
            bindings = new Bindings();
            bindings.put(SOURCE, source);
            bindings.put(TARGET, target);
            bindings.put(SPELLS, game.spells());
            bindings.put(GAME, game);
            bindings.put(DAMAGE_TYPE, DamageType.class);
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
