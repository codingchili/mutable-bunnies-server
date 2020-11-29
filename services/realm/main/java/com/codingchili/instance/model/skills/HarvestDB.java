package com.codingchili.instance.model.skills;

import com.codingchili.core.context.CoreContext;
import com.codingchili.instance.model.npc.DB;

import java.util.Map;
import java.util.Optional;

class HarvestDB {
    private static final String CONF_PATH = "conf/game/harvesting";
    private static DB<HarvestConfig> harvestable;

    /**
     *
     * @param game
     */
    public HarvestDB(CoreContext game) {
        harvestable = DB.create(game, HarvestConfig.class, CONF_PATH);
    }

    public Optional<HarvestConfig> getById(String id) {
        return harvestable.getById(id);
    }

    public Map<String, HarvestConfig> all() {
        return harvestable.asMap();
    }
}
