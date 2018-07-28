package com.codingchili.realm.instance.model.stats;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robin Duda
 *
 * Stats is a form of attribute values that may be applied to a
 * character or an item.
 */
public class Stats extends ConcurrentHashMap<Attribute, Float> {

    /**
     * Adds the given value to the attribute if existing or sets it if not.
     * @param type the type of attribute to update.
     * @param points the amount to modify, may be negative.
     * @return fluent.
     */
    public Stats update(Attribute type, float points) {
        float current = getOrDefault(type, 0f) + points;
        put(type, current);
        return this;
    }

    /**
     * Set the attribute value ignoring any previous values.
     * @param type the type of attribute to set.
     * @param value the value to set.
     * @return fluent.
     */
    public Stats set(Attribute type, float value) {
        put(type, value);
        return this;
    }

    /**
     * Retrieves the value of the specified attribute.
     * @param attribute the attribute to retrieve the value of.
     * @return the value of the given attribute, default 0.
     */
    public float get(Attribute attribute) {
        return getOrDefault(attribute, 0f);
    }

    /**
     * Merges the current attributes with the given set of attributes.
     * @param stats a set of attribute to merge into this.
     * @return the merged attributes.
     */
    public Stats apply(Stats stats) {
        stats.forEach(this::update);
        return this;
    }
}
