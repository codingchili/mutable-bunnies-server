package com.codingchili.instance.model.stats;

import java.util.LinkedHashMap;

/**
 * @author Robin Duda
 * <p>
 * Stats is a form of attribute values that may be applied to a
 * character or an item.
 */
public class Stats extends LinkedHashMap<Attribute, Double> {

    /**
     * Adds the given value to the attribute if existing or sets it if not.
     *
     * @param type   the type of attribute to update.
     * @param points the amount to modify, may be negative.
     * @return fluent.
     */
    public Stats update(Attribute type, double points) {
        double current = getOrDefault(type, 0.0) + points;
        put(type, current);
        return this;
    }

    /**
     * Set the attribute value ignoring any previous values.
     *
     * @param type  the type of attribute to set.
     * @param value the value to set.
     * @return fluent.
     */
    public Stats set(Attribute type, double value) {
        put(type, value);
        return this;
    }

    /**
     * Retrieves the value of the specified attribute.
     *
     * @param attribute the attribute to retrieve the value of.
     * @return the value of the given attribute, default 0.
     */
    public double get(Attribute attribute) {
        return getOrDefault(attribute, 0.0);
    }

    /**
     * Sets the attribute to the given default value if unset.
     *
     * @param attribute the attribute to update.
     * @param value     the value to set if none exists.
     */
    public void setDefault(Attribute attribute, double value) {
        putIfAbsent(attribute, value);
    }

    /**
     * Merges the current attributes with the given set of attributes.
     *
     * @param stats a set of attribute to merge into this.
     * @return the merged attributes.
     */
    public Stats apply(Stats stats) {
        stats.forEach(this::update);
        return this;
    }
}
