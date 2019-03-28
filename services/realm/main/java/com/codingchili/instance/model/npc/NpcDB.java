package com.codingchili.instance.model.npc;

import java.util.Optional;

import com.codingchili.core.context.CoreContext;

/**
 * Database of all NPC configured.
 */
public class NpcDB {
    private static final String CONF_PATH = "conf/game/npc";
    private static DB<EntityConfiguration> npcs;

    /**
     * Creates a new or re-uses an existing npc database.
     *
     * @param core the core context to create the database on.
     */
    public NpcDB(CoreContext core) {
        npcs = DB.create(core, EntityConfiguration.class, CONF_PATH);
    }

    public Optional<EntityConfiguration> getById(String id) {
        return npcs.getById(id);
    }
}
