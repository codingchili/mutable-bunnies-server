package com.codingchili.instance.model.skills;

import com.codingchili.core.context.CoreContext;
import com.codingchili.instance.model.npc.DB;

import java.util.Map;
import java.util.Optional;

class SkillDB {
    private static final String CONF_PATH = "conf/game/skills";
    private static DB<SkillConfig> skills;

    /**
     *
     * @param game
     */
    public SkillDB(CoreContext game) {
        skills = DB.create(game, SkillConfig.class, CONF_PATH);
    }

    public Optional<SkillConfig> getById(String id) {
        return skills.getById(id);
    }

    public Map<String, SkillConfig> all() {
        return skills.asMap();
    }
}
