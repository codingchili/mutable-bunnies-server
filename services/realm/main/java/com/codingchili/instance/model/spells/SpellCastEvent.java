package com.codingchili.instance.model.spells;

import com.codingchili.instance.model.events.*;

/**
 * @author Robin Duda
 * <p>
 * Emitted when a new spell is casted.
 */
public class SpellCastEvent implements Event {
    private SpellState state;
    private ActiveSpell spell;

    public SpellCastEvent(ActiveSpell spell) {
        this.spell = spell;
        this.state = spell.getSource().getSpells();
    }

    public String getSpell() {
        return spell.getSpell().getId();
    }

    public SpellCastEvent setSpell(ActiveSpell spell) {
        this.spell = spell;
        return this;
    }

    public SpellCycle getCycle() {
        return spell.getCycle();
    }

    public SpellTarget getSpellTarget() {
        return spell.getTarget();
    }

    public Float getCastTime() {
        return spell.getSpell().getCasttime();
    }

    public int getCharges() {
        return state.charges(spell.getSpell());
    }

    public long getCooldown() {
        return state.cooldown(spell.getSpell());
    }

    public long getGcd() {
        return state.getGcd();
    }

    @Override
    public String getSource() {
        return spell.getSource().getId();
    }

    @Override
    public EventType getRoute() {
        return EventType.spell;
    }

    @Override
    public Broadcast getBroadcast() {
        return Broadcast.PARTITION;
    }
}
