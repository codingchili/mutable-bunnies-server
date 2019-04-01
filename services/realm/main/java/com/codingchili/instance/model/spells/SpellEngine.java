package com.codingchili.instance.model.spells;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.Ticker;
import com.codingchili.instance.model.afflictions.ActiveAffliction;
import com.codingchili.instance.model.afflictions.AfflictionDB;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Grid;
import com.codingchili.instance.model.events.*;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.model.stats.Stats;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages spell casting, afflictions and damaging.
 * <p>
 * Todo: add more callbacks for afflictions
 * - on heal
 * - on damage
 * - on interrupt etc..
 */
public class SpellEngine {
    private Map<Creature, ActiveSpell> casting = new ConcurrentHashMap<>();
    private Collection<ActiveSpell> active = new ConcurrentLinkedQueue<>();
    private Collection<Projectile> projectiles = new ConcurrentLinkedQueue<>();
    private Grid<Creature> creatures;
    private AfflictionDB afflictions;
    private SpellDB spells;
    private GameContext game;

    /**
     * Creates a new spell engine on the given game context.
     *
     * @param game the game context the spell engine is to be attached to.
     */
    public SpellEngine(GameContext game) {
        this.game = game;
        this.creatures = game.creatures();
        this.spells = new SpellDB(game.instance());
        this.afflictions = new AfflictionDB(game.instance());

        game.ticker(this::tick, GameContext.secondsToTicks(0.1));
    }

    /**
     * Attempts to cast a new spell.
     *
     * @param caster    the caster casting the spell.
     * @param target    the spelltarget, a single target, aoe, cone etc.
     * @param spellName the name of the spell to cast.
     * @return a spell result indicating if the spell may be casted.
     */
    public SpellResult cast(Creature caster, SpellTarget target, String spellName) {
        Optional<Spell> spell = spells.getByName(spellName);

        if (spell.isPresent()) {

            if (caster.getSpells().learned(spellName)) {
                if (caster.getSpells().isOnCooldown(spell.get())) {
                    return SpellResult.COOLDOWN;
                } else {
                    ActiveSpell active = new ActiveSpell(spell.get())
                            .setSource(caster)
                            .setTarget(target);

                    if (active.onCastBegin(game)) {
                        cancel(caster);
                        casting.put(active.getSource(), active);
                        game.publish(new SpellCastEvent(active));
                        return SpellResult.CASTING;
                    } else {
                        return SpellResult.UNABLE;
                    }
                }
            } else {
                return SpellResult.UNKNOWN_SPELL;
            }
        } else {
            throw new NoSuchSpellException(spellName);
        }
    }

    /**
     * @param caster the caster to cancel all pending spells for.
     */
    public void cancel(Creature caster) {
        ActiveSpell spell = casting.get(caster);
        if (spell != null) {
            game.publish(new SpellCastEvent(spell.setCycle(SpellCycle.CANCELLED)));
            casting.remove(caster);
        }
    }

    /**
     * @param caster the caster to interrupt spellcasting for.
     */
    public void interrupt(Creature caster) {
        ActiveSpell spell = casting.get(caster);
        if (spell != null) {
            game.publish(new SpellCastEvent(spell.setCycle(SpellCycle.INTERRUPTED)));
            casting.remove(caster);
        }
    }

    /**
     * @param target the target to remove the given affliction of, or any.
     * @param regex  a regex matching the name of afflictions to remove.
     */
    public void cleanse(Creature target, String regex) {
        target.getAfflictions().removeIf(affliction ->
                        affliction.getAffliction().getName().matches(regex), game);
    }

    /**
     * Adds an affliction to the given target using itself as the source.
     *
     * @param source     the source and target of the affliction.
     * @param affliction the name of the affliction to apply.
     */
    public void afflict(Creature source, String affliction) {
        afflict(source, source, affliction);
    }

    /**
     * Adds a charge to the spell with the given name if not at max charges.
     *
     * @param caster  the caster to add the charge to.
     * @param spellId the id of the spell to add a charge to.
     */
    public void charge(Creature caster, String spellId) {
        spells.getByName(spellId).ifPresent(spell -> {
            caster.getSpells().charge(spell, 1.0f);
            caster.handle(new SpellStateEvent(caster.getSpells(), spell));
        });
    }

    /**
     * Adds some experience points to the given target.
     *
     * @param target the receiver of the experience points.
     * @param amount the number of experience points to add.
     */
    public void experience(Creature target, int amount) {
        Stats stats = target.getBaseStats();

        while (amount > 0) {
            Double current = stats.get(Attribute.experience);
            Double next = stats.get(Attribute.nextlevel) - current;

            if (next.intValue() < amount) {
                amount -= next.intValue();
                stats.update(Attribute.level, 1);
                stats.set(Attribute.experience, 0);
                stats.set(Attribute.nextlevel, getNextLevelExp(target));
            } else {
                stats.update(Attribute.experience, amount);
                amount = 0;
            }
        }
        target.handle(new StatsUpdateEvent(target));
    }

    private Double getNextLevelExp(Creature target) {
        Bindings bindings = new Bindings()
                .setSource(target)
                .setAttribute(Attribute.class);

        Scripted script = game.instance().realm().getLevelScaling();

        return script.apply(bindings);
    }

    /**
     * Applies an affliction to the given target.
     *
     * @param source the source creature that applied the affliction.
     * @param target the target creature that is afflicted.
     * @param name   the name of the affliction to apply.
     */
    public void afflict(Creature source, Creature target, String name) {
        afflictions.getByName(name).ifPresent(affliction -> {
            ActiveAffliction active = affliction.apply(source, target);
            source.getAfflictions().add(active, game);
            game.publish(new AfflictionEvent(active));
        });
    }

    /**
     * Reduces the energy of the given target, may be called in the onCast method in spells.
     *
     * @param target the target to reduce the available energy of.
     * @param amount the amount of energy to deduct.
     * @return true if the energy was available and consumed, otherwise false.
     */
    public boolean energy(Creature target, int amount) {
        Stats stats = target.getBaseStats();

        if (amount < 0 && stats.get(Attribute.energy) < amount) {
            // cannot update energy to negative values.
            return false;
        } else {
            if (amount > stats.get(Attribute.maxenergy)) {
                // prevent updating over maximum.
                amount = (int) stats.get(Attribute.maxenergy);
            }

            stats.update(Attribute.energy, amount);
            target.handle(new StatsUpdateEvent(target));
            return true;
        }
    }

    /**
     * Heals the given target by applying more health. Targets may not be healed
     * above the maximum health.
     *
     * @param target the target to apply the health to.
     * @param value  the amount of health to apply, may not exceed the max health of the being.
     */
    public void heal(Creature target, double value) {
        double max = target.getStats().get(Attribute.maxhealth);
        double current = target.getBaseStats().get(Attribute.health);

        if (current + value > max) {
            value = Math.max(0, max - current);
        }
        target.getBaseStats().update(Attribute.health, value);
        game.publish(new DamageEvent(target, value, DamageType.heal));
    }

    /**
     * @param spell      the active spell that spawned the projectile. If the spell
     *                   has defined a callback for onHit then this will be called
     *                   each time a projectile hits a target.
     */
    public Projectile projectile(ActiveSpell spell) {
        Projectile projectile = new Projectile(game, spell);
        projectiles.add(projectile);
        return projectile;
    }

    /**
     * Retrieves a spell given its name. If the spell does not exist an error
     * will be thrown.
     *
     * @param spellName the name of the spell to retrieve.
     * @return a spell matching the given spell name.
     */
    public Optional<Spell> getSpellByName(String spellName) {
        return spells.getByName(spellName);
    }


    /**
     * Checks if the given spell name is registered in the spell engine.
     *
     * @param spellName the name of the spell.
     * @return true if the spell is registered, otherwise false.
     */
    public boolean exists(String spellName) {
        return spells.getByName(spellName).isPresent();
    }

    /**
     * Damages the given creature using the given value and damage type.
     *
     * @param source the source that applies the damage to the target.
     * @param target the target the damage is to be applied to.
     * @param value  the amount of damage to apply.
     * @param type   the type of damage to apply, this may be healing as well..
     */
    public void damage(Creature source, Creature target, double value, DamageType type) {
        target.getBaseStats().update(Attribute.health, (int) value);

        game.publish(new DamageEvent(target, value, type).setSource(source));

        if (target.getStats().get(Attribute.health) < 0) {
            game.publish(new DeathEvent(target, source));

            // despawn the player: requires the player to issue a "join" to get re-spawned.
            game.remove(target);
        }
    }

    private void tick(Ticker ticker) {
        updateCreatureSpellState(ticker);
        updateCastingProgress(ticker);
        updateActiveSpells(ticker);
        updateProjectiles(ticker);
    }

    // update affliction state and spell cooldowns.
    private void updateCreatureSpellState(Ticker ticker) {
        float delta = ticker.delta();
        creatures.all().forEach(entity -> {
            entity.getAfflictions().removeIf(active -> {
                if (active.shouldTick(delta))
                    return !active.tick(game);
                return false;
            }, game);

            entity.getSpells().tick(entity, spells, delta);
        });
    }

    // update progress for spells currently being casted.
    private void updateCastingProgress(Ticker ticker) {
        float delta = ticker.delta();
        casting.values().removeIf((casting) -> {
            if (casting.completed(delta)) {
                game.publish(new SpellCastEvent(casting.setCycle(SpellCycle.CASTED)));
                casting.onCastCompleted(game);

                // the spell is casted: stay active until the spell expires.
                active.add(casting);
                return true;
            } else {
                if (casting.shouldTick(delta)) {
                    casting.onCastProgress(game);
                }
            }
            return false;
        });
    }

    // execute spell effects for spells that have been casted successfully.
    private void updateActiveSpells(Ticker ticker) {
        float delta = ticker.delta();
        active.removeIf(spell -> {
            if (spell.active(delta)) {

                if (spell.shouldTick(delta)) {
                    spell.onSpellEffects(game);
                }

                return false;
            } else {
                return true;
            }
        });
    }

    // updates all projectiles.
    private void updateProjectiles(Ticker ticker) {
        projectiles.removeIf((projectile) -> projectile.tick(ticker));
    }
}
