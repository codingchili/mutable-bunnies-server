package com.codingchili.instance.model.requests;

import com.codingchili.instance.model.spells.SpellResult;
import com.codingchili.instance.model.spells.SpellTarget;

/**
 * @author Robin Duda
 *
 * A response to a spell cast request.
 */
public class SpellCastResponse {
    private SpellResult result;
    private SpellTarget target;
    private String spellId;

    public SpellCastResponse(SpellResult result, SpellCastRequest cast) {
        this.result = result;
        this.spellId = cast.getSpellId();
        this.target = cast.getSpellTarget();
    }

    public SpellTarget getSpelltarget() {
        return target;
    }

    public String getSpellId() {
        return spellId;
    }

    public SpellResult getResult() {
        return result;
    }

    public void setResult(SpellResult result) {
        this.result = result;
    }
}
