package com.codingchili.instance.model.skills;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.entity.SimpleEntity;
import com.codingchili.instance.scripting.Bindings;

import java.util.Map;
import java.util.Optional;

/**
 * Manages player skills.
 */
public class SkillEngine {
    private GameContext game;
    private SkillDB skills;
    private HarvestDB harvesting;

    public SkillEngine(GameContext game) {
        this.skills = new SkillDB(game.instance());
        this.harvesting = new HarvestDB(game.instance());
        this.game = game;
    }

    public void experience(PlayerCreature target, SkillType skill, final int total) {
        SkillConfig config = skills.all().get(skill.name());
        SkillProgress state = target.getSkills().get(skill);
        boolean levelUp = false;
        int amount = total;

        while (amount > 0) {
            int exp = state.getExperience();
            int next = state.getNextlevel() - exp;

            if (next < amount) {
                amount -= next;
                Bindings bindings = bindings(state.getLevel() + 1);
                state.levelUp(config.getScaling().apply(bindings));
                levelUp = true;
            } else {
                state.setExperience(exp + amount);
                amount = 0;
            }
        }
        target.handle(new SkillChangeEvent(state)
                .setLevelup(levelUp)
                .setExperience(total)
        );
    }

    private Bindings bindings(int nextLevel) {
        return new Bindings()
                .set("level", nextLevel);
    }

    public HarvestConfig harvestById(String id) {
        return harvesting.getById(id)
                .orElseThrow(() -> new HarvestConfigNotFound(id));
    }

    public SkillConfig skillById(SkillType type) {
        return skills.getById(type.name())
                .orElseThrow(() -> new SkillConfigNotFound(type));
    }

    public Optional<SkillConfig> details(SkillType type) {
        return skills.getById(type.name());
    }

    public void register(SimpleEntity entity, String harvest) {
        HarvestConfig config = harvestById(harvest);
        switch (config.getSkill()) {
            case mining:
                entity.getInteractions().add(SkillType.mining.name());
                break;
            case farming:
                entity.getInteractions().add(SkillType.farming.name());
                break;
        }
    }
}
