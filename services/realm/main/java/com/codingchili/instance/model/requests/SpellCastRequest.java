package com.codingchili.instance.model.requests;

import com.codingchili.instance.model.spells.SpellTarget;

/**
 * @author Robin Duda
 * <p>
 * Request from a client to cast a spell.
 */
public class SpellCastRequest {
    private SpellTarget spellTarget;
    private String spellId;

    public SpellTarget getSpellTarget() {
        return spellTarget;
    }

    public String getSpellId() {
        return spellId;
    }

    public void setSpellTarget(SpellTarget spellTarget) {
        this.spellTarget = spellTarget;
    }

    public void setSpellId(String spellId) {
        this.spellId = spellId;
    }
}
