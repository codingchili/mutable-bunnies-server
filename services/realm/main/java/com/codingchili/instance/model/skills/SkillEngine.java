package com.codingchili.instance.model.skills;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.scripting.Bindings;

import java.util.Map;

/**
 *
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

    public void experience(PlayerCreature target, SkillType skill, int amount) {
        SkillConfig config = skills.all().get(skill.name());
        LearnedSkill state = target.getSkills().get(skill);

        while (amount > 0) {
            int exp = state.getExperience();
            int next = state.getNextlevel() - exp;

            if (next < amount) {
                amount -= next;
                Bindings bindings = bindings(state.getLevel() + 1);
                state.levelUp(config.getScaling().apply(bindings));
                target.handle(new SkillExpEvent()); // todo: set levelup true.
            } else {
                state.setExperience(exp + amount);
                amount = 0;
                target.handle(new SkillExpEvent());
            }
        }
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
                .orElseThrow(() -> new SkillConfigNotFound(type.name()));
    }

    public Map<String, SkillConfig> details() {
        return skills.all();
    }
}
