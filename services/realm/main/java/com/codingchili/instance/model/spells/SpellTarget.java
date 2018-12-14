package com.codingchili.instance.model.spells;

import com.codingchili.instance.model.entity.Vector;

/**
 * @author Robin Duda
 * <p>
 * A spell target is required to cast spells.
 * A target can be either a vector or a single creature, depending on the spell.
 */
public class SpellTarget {
    private Vector vector;
    private String targetId;

    public Vector getVector() {
        return vector;
    }

    public SpellTarget setVector(Vector vector) {
        this.vector = vector;
        return this;
    }

    public String getTargetId() {
        return targetId;
    }

    public SpellTarget setTargetId(String targetId) {
        this.targetId = targetId;
        return this;
    }
}
