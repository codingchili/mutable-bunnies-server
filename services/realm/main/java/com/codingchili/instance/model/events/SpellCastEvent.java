package com.codingchili.instance.model.events;

import com.codingchili.instance.model.spells.ActiveSpell;
import com.codingchili.instance.model.spells.SpellTarget;

/**
 * @author Robin Duda
 *
 * Emitted when a new spell is casted.
 */
public class SpellCastEvent implements Event {
    private ActiveSpell spell;

    public SpellCastEvent(ActiveSpell spell) {
        this.spell = spell;
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

    public SpellTarget getTarget() {
        return spell.getTarget();
    }

    public Float getCastTime() {
        return spell.getSpell().getCasttime();
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
