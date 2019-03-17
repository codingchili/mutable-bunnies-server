package com.codingchili.instance.model.entity;

/**
 * Supported types of hitboxes.
 */
public enum HitboxType {
    /**
     * Requires two points to set up a rectangle.
     */
    rectangular,

    /**
     * Requires a list of points that make up a form.
     */
    freeform
}
