package com.codingchili.realm.instance.model.requests;

import com.codingchili.realm.instance.model.spells.SpellTarget;

/**
 * @author Robin Duda
 * <p>
 * Request from a client to cast a spell.
 */
public class SpellCastRequest {
    private SpellTarget spellTarget;
    private String spellName;

    public SpellTarget getSpellTarget() {
        return spellTarget;
    }

    public String getSpellName() {
        return spellName;
    }

    public void setSpellTarget(SpellTarget spellTarget) {
        this.spellTarget = spellTarget;
    }

    public void setSpellName(String spellName) {
        this.spellName = spellName;
    }
}
