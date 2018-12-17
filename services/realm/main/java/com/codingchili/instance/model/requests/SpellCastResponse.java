package com.codingchili.instance.model.requests;

import com.codingchili.instance.model.spells.SpellResult;

/**
 * @author Robin Duda
 */
public class SpellCastResponse {
    private SpellResult result;

    public SpellCastResponse(SpellResult result) {
        this.result = result;
    }

    public SpellResult getResult() {
        return result;
    }

    public void setResult(SpellResult result) {
        this.result = result;
    }
}