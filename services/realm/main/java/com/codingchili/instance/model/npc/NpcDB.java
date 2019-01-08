package com.codingchili.instance.model.npc;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 */
public class NpcDB {
    private static final String CONF_PATH = "conf/game/npc";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static DB<NpcConfiguration> npcs;

    public NpcDB(CoreContext core) {
        if (!initialized.getAndSet(true)) {
            npcs = new DB<>(core, NpcConfiguration.class, CONF_PATH);
        }
    }

    public Optional<NpcConfiguration> getById(String id) {
        return npcs.getById(id);
    }
}
