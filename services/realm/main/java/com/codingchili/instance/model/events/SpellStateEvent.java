package com.codingchili.instance.model.events;

import com.codingchili.instance.model.spells.Spell;
import com.codingchili.instance.model.spells.SpellState;

/**
 * @author Robin Duda
 *
 * Emitted when the spell state is changed, for example a charge is added or
 * a spell is learned.
 */
public class SpellStateEvent implements Event {
    private SpellState state;
    private Spell spell;

    public SpellStateEvent(SpellState state, Spell spell) {
        this.state = state;
        this.spell = spell;
    }

    public int getCharges() {
        return state.charges(spell);
    }

    public long getCooldown() {
        return state.cooldown(spell);
    }

    public String getSpell() {
        return spell.getId();
    }

    @Override
    public EventType getRoute() {
        return EventType.spellstate;
    }
}
