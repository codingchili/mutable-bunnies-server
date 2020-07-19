package com.codingchili.instance.model.npc;

import com.codingchili.instance.model.entity.SimpleEntity;

/**
 * @author Robin Duda
 *
 * Concrete class for structures.
 */
public class Structure extends SimpleEntity {
    private EntityConfig config;

    public Structure(EntityConfig config) {
        this.config = config;
    }
}
