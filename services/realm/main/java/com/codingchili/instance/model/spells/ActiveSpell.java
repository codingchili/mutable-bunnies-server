package com.codingchili.instance.model.spells;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.events.SpellCycle;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.scripting.Bindings;

import java.util.function.Consumer;

import com.codingchili.core.logging.Level;

import static com.codingchili.common.Strings.ID_LOG;
import static com.codingchili.common.Strings.ID_NAME;

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
    private int progress = 0;
    private int timer;
    private Creature source;
    private SpellTarget target;
    private Spell spell;

    public ActiveSpell(Spell spell) {
        this.timer = GameContext.secondsToTicks(spell.getActive());
        this.progress = GameContext.secondsToTicks(spell.getCasttime());
        this.spell = spell;
    }

    public boolean completed() {
        return (--progress <= 0);
    }

    public boolean active() {
        return (--timer <= 0);
    }

    public void onCastProgress(GameContext game, int tick) {
        if (spell.onCastProgress != null) {
            Bindings bindings = getBindings(game);
            bindings.put(TICK, tick);
            try {
                spell.onCastProgress.apply(bindings);
            } catch (Throwable e) {
                game.getLogger(getClass()).onError(e);
            }
        }
    }

    public void onCastCompleted(GameContext game) {
        if (spell.onCastComplete != null) {
            try {
                spell.onCastComplete.apply(getBindings(game));
            } catch (Throwable e) {
                game.getLogger(getClass()).onError(e);
            }
        }
    }

    public boolean onCastBegin(GameContext game) {
        source.getSpells().setCooldown(spell);
        if (spell.onCastBegin != null) {
            return spell.onCastBegin.apply(getBindings(game));
        } else {
            return true;
        }
    }

    public void onSpellEffects(GameContext game, int tick) {
        if (spell.onSpellActive != null) {
            Bindings bindings = getBindings(game);
            bindings.put(TICK, tick);
            spell.onSpellActive.apply(bindings);
        }
    }

    public boolean shouldTick(long currentTick) {
        return currentTick % spell.getInterval() == 0;
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
