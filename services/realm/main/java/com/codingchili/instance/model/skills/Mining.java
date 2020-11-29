package com.codingchili.instance.model.skills;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.npc.Structure;
import com.codingchili.instance.model.spells.ActiveSpell;
import com.codingchili.instance.model.spells.SpellStage;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.NativeScript;
import com.codingchili.instance.scripting.Scripted;

import java.util.Objects;
import java.util.Random;

public class Mining implements Scripted {
    private static final Integer MAX_RANGE = 120;
    private Random random = new Random();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T apply(Bindings bindings) {
        SpellStage stage = bindings.retrieve(ActiveSpell.STAGE);
        GameContext game = bindings.getContext();
        PlayerCreature player = bindings.getSource();
        Structure target = bindings.getTarget();

        switch (stage) {
            case BEGIN:
                return (T) onCastBegin(game, player, target);
            case PROGRESS:
                onCastProgress(game, player, target);
                break;
            case COMPLETED:
                onCastComplete(game, player, target);
                break;
        }
        return null;
    }

    public Boolean onCastBegin(GameContext game, PlayerCreature player, Structure structure) {
        SkillEngine skills = game.skills();
        String resource = structure.getConfig().getHarvest();

        // ensure target in range and has a resource.
        boolean range = player.getVector().distance(structure.getVector()) < MAX_RANGE;
        boolean harvestable = resource != null;

        if (range) {
            if (harvestable) {
                SkillConfig config = skills.skillById(SkillType.mining);
                boolean learned = player.getSkills().learned(SkillType.mining);
                if (learned) {
                    LearnedSkill skill = player.getSkills().get(SkillType.mining);
                    int level = skill.getLevel();
                    boolean skilled = config.getPerks().stream()
                            .filter(perk -> perk.getId().equals(resource))
                            .anyMatch(perk -> perk.getLevel() <= level);

                    if (skilled) {
                        return true;
                    } else {
                        levelNotHighEnough();
                    }
                } else {
                    skillNotLearned();
                }
            } else {
                targetNotHarvestable();
            }
        } else {
            targetOutOfRange();
        }
        return false;
    }

    public void onCastProgress(GameContext game, PlayerCreature player, Structure target) {
        SkillEngine skills = game.skills();
        HarvestConfig harvest = skills.harvestById(target.getConfig().getHarvest());
        LearnedSkill learned = player.getSkills().get(SkillType.mining);
        float effectiveness = skills.skillById(SkillType.mining)
                .getPerks().stream()
                .filter(perk -> perk.getLevel() <= learned.getLevel())
                .map(Perk::getEffectiveness)
                .filter(Objects::nonNull)
                .reduce(1f, Float::sum);

        // effectiveness affects drop rate and failure rate.
        boolean success = random.nextFloat() < harvest.getSuccess() * effectiveness;
        boolean failure = random.nextFloat() < harvest.getFail() * effectiveness;

        if (success) {
            game.inventory().items().getById(harvest.getId()).ifPresent(item -> {
                game.inventory().drop(target.getVector(), item);
            });
        }

        if (failure) {
            // damage the player, drain players energy, afflict with something, break item in inventory etc.
            // send a chat message event from the structure
        }
    }

    public void onCastComplete(GameContext game, PlayerCreature player, Structure target) {
        SkillEngine skills = game.skills();
        HarvestConfig harvest = skills.harvestById(target.getConfig().getHarvest());
        skills.experience(player, SkillType.mining, harvest.getExperience());
    }

    private void targetOutOfRange() {
        throw new CoreRuntimeException("Target out of range.");
    }

    private void targetNotHarvestable() {
        throw new CoreRuntimeException("Target is not harvestable.");
    }

    private void skillNotLearned() {
        throw new CoreRuntimeException("The %s skill is not yet learned.");
    }

    private void levelNotHighEnough() {
        throw new CoreRuntimeException("%s level not high enough.");
    }

    @Override
    public String getEngine() {
        return NativeScript.TYPE;
    }

    @Override
    public String getSource() {
        return Mining.class.getName();
    }
}
