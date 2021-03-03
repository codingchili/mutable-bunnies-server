package com.codingchili.instance.model.skills;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Entity;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.npc.Structure;
import com.codingchili.instance.model.spells.*;
import com.codingchili.instance.scripting.Bindings;

import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * Implementation of the Mining spell in Java.
 */
public class Mining<T> implements Function<Bindings, Boolean> {
    private static final Integer MAX_RANGE = 120;
    private Random random = new Random();

    @Override
    public Boolean apply(Bindings bindings) {
        SpellStage stage = bindings.retrieve(ActiveSpell.STAGE);
        GameContext game = bindings.getContext();
        PlayerCreature player = bindings.getSource();
        SpellTarget spellTarget = bindings.getTarget();
        Structure target = game.getById(spellTarget.getTargetId());

        switch (stage) {
            case BEGIN:
                return onCastBegin(game, player, target);
            case PROGRESS:
                onCastProgress(game, player, target);
                break;
            case COMPLETED:
                onCastComplete(game, player, target);
                break;
        }
        return false;
    }

    public Boolean onCastBegin(GameContext game, PlayerCreature player, Structure structure) {
        SkillEngine skills = game.skills();
        String resource = structure.getConfig().getHarvest();
        SkillConfig config = skills.skillById(SkillType.mining);
        SkillProgress skill = player.getSkills().get(SkillType.mining);

        // ensure target in range and has a resource.
        boolean range = player.getVector().distance(structure.getVector()) < MAX_RANGE;
        boolean harvestable = resource != null;

        if (range) {
            if (harvestable) {
                int level = skill.getLevel();
                boolean skilled = config.getPerks().stream()
                        .filter(perk -> perk.getId().equals(resource))
                        .anyMatch(perk -> perk.getLevel() <= level);

                if (skilled) {
                    return true;
                } else {
                    levelNotHighEnough(skill.getType());
                }
            } else {
                targetNotHarvestable(structure);
            }
        } else {
            targetOutOfRange(structure);
        }
        return false;
    }

    public void onCastProgress(GameContext game, PlayerCreature player, Structure target) {
        SkillEngine skills = game.skills();
        HarvestConfig harvest = skills.harvestById(target.getConfig().getHarvest());
        SkillProgress learned = player.getSkills().get(SkillType.mining);
        float effectiveness = skills.skillById(SkillType.mining)
                .getPerks().stream()
                .filter(perk -> perk.getLevel() <= learned.getLevel())
                .map(Perk::getEffectiveness)
                .filter(Objects::nonNull)
                .reduce(1f, Float::sum);

        // effectiveness affects drop rate and failure rate.
        float roll = random.nextFloat();
        boolean success = roll < harvest.getSuccess() * effectiveness;
        boolean failure = roll < harvest.getFail() * (1.0f / effectiveness);

        if (success) {
            game.inventory().items().getById(harvest.getId()).ifPresent(item -> {
                game.inventory().drop(target.getVector(), item);
            });
        }

        if (failure) {
            // damage the player, drain players energy, afflict with something, break item in inventory etc.
            // send a chat message event from the structure
            game.getLogger(getClass()).log("failed mining skill: make bad things happen.");
        }
    }

    public void onCastComplete(GameContext game, PlayerCreature player, Structure target) {
        SkillEngine skills = game.skills();
        HarvestConfig harvest = skills.harvestById(target.getConfig().getHarvest());
        skills.experience(player, SkillType.mining, harvest.getExperience());
    }

    private void targetOutOfRange(Entity target) {
        throw new CoreRuntimeException(String.format("%s out of range.", target.getName()));
    }

    private void targetNotHarvestable(Entity target) {
        throw new CoreRuntimeException(String.format("%s is not harvestable.", target.getName()));
    }

    private void levelNotHighEnough(SkillType skill) {
        throw new CoreRuntimeException(String.format("%s level not high enough.", skill.name()));
    }
}
