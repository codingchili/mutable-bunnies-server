package com.codingchili.realm.instance.model.requests;

import com.codingchili.realm.instance.model.spells.SpellTarget;

/**
 * @author Robin Duda
 * <p>
 * Request from a client to cast a spell.
 */
public class SpellCastRequest {
    private SpellTarget target;
    private String spellName;

    public SpellTarget getTarget() {
        return target;
    }

    public String getSpellName() {
        return spellName;
    }

    public void setTarget(SpellTarget target) {
        this.target = target;
    }

    public void setSpellName(String spellName) {
        this.spellName = spellName;
    }
}
