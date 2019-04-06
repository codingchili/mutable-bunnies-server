package com.codingchili.instance.model.spells;

import com.codingchili.instance.model.entity.Vector;

import java.util.Objects;

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
        Objects.requireNonNull(vector, "target vector is not set in spell target.");
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
