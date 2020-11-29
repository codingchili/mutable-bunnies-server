package com.codingchili.instance.model.npc;

import com.codingchili.instance.model.entity.SimpleEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @JsonIgnore
    public EntityConfig getConfig() {
        return config;
    }

    @Override
    public boolean isCreature() {
        return false;
    }
}
