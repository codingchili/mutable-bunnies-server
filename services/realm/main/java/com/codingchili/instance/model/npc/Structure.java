package com.codingchili.instance.model.npc;

import com.codingchili.instance.model.entity.SimpleEntity;

/**
 * @author Robin Duda
 *
 * Concrete class for structures.
 */
public class Structure extends SimpleEntity {
    private EntityConfiguration config;

    public Structure(EntityConfiguration config) {
        this.config = config;
    }
}
