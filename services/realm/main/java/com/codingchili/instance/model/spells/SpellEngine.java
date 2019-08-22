package com.codingchili.instance.model.spells;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.Ticker;
import com.codingchili.instance.model.afflictions.*;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.items.StatsUpdateEvent;
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
    private static final int XP_PER_CREATURE_LEVEL = 15;
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
     * @param caster  the caster casting the spell.
     * @param target  the spelltarget, a single target, aoe, cone etc.
     * @param spellId the name of the spell to cast.
     * @return a spell result indicating if the spell may be casted.
     */
    public SpellResult cast(Creature caster, SpellTarget target, String spellId) {
        Optional<Spell> spell = spells.getById(spellId);

        if (spell.isPresent()) {

            if (caster.getSpells().learned(spellId)) {
                if (caster.getSpells().isOnCooldown(spell.get())) {
                    return SpellResult.COOLDOWN;
                } else {
                    ActiveSpell active = new ActiveSpell(spell.get())
                            .setSource(caster)
                            .setTarget(target);

                    if (active.onCastBegin(game)) {
                        cancel(caster);
                        casting.put(active.getSource(), active);

                        // restrict movement speed while casting.
                        if (!spell.get().getMobile()) {
                            game.movement().stop(caster);
                        }

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
            throw new NoSuchSpellException(spellId);
        }
    }

    /**
     * @return affliction database.
     */
    public AfflictionDB afflictions() {
        return afflictions;
    }

    /**
     * Checks if the given target is casting a spell.
     *
     * @param caster the caster.
     * @return an optional with the spell being casted, otherwise empty.
     */
    public Optional<ActiveSpell> casting(Creature caster) {
        if (casting.containsKey(caster)) {
            return Optional.of(casting.get(caster));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Checks if the given creature is movement impaired due to spellcasting.
     *
     * @param creature the caster casting the spell.
     * @return true if the caster is not casting a spell or is casting a mobile spell.
     */
    public boolean mobile(Creature creature) {
        Optional<ActiveSpell> spell = casting(creature);

        // should we check if the given creature is movement impaired by an affliction?

        if (spell.isPresent()) {
            return spell.get().getSpell().getMobile();
        } else {
            // not casting a spell: creature is mobile.
            return true;
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
        List<String> removed = new ArrayList<>();

        target.getAfflictions().removeIf(affliction -> {
            boolean matches = affliction.getAffliction().getName().matches(regex);
            if (matches) {
                removed.add(affliction.getAffliction().getName());
            }
            return matches;
        }, game);

        if (removed.size() > 0) {
            game.publish(new CleanseEvent(target, removed));
        }
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
        spells.getById(spellId).ifPresent(spell -> {
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
            double current = stats.get(Attribute.experience);
            double next = stats.get(Attribute.nextlevel) - current;

            if ((int) next < amount) {
                amount -= (int) next;
                stats.update(Attribute.level, 1);
                stats.set(Attribute.experience, 0);
                stats.set(Attribute.nextlevel, getNextLevelExp(target));

                // set max energy/hp on level up.
                Stats computed = target.getStats();
                stats.set(Attribute.health, computed.get(Attribute.maxhealth));
                stats.set(Attribute.energy, computed.get(Attribute.maxenergy));
            } else {
                stats.update(Attribute.experience, amount);
                amount = 0;
            }
        }
        game.publish(new StatsUpdateEvent(target));
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
        afflictions.getById(name).ifPresent(affliction -> {
            ActiveAffliction active = affliction.apply(source, target);
            target.getAfflictions().add(active, game);
            game.publish(new AfflictionEvent(target, active));
        });
    }

    /**
     * Reduces the energy of the given target, may be called in the onCast method in spells.
     *
     * @param target the target to reduce the available energy of.
     * @param amount the amount of energy to deduct.
     * @return true if the energy was available and consumed, otherwise false. Always returns
     * true when energy is added.
     */
    public boolean energy(Creature target, double amount) {
        double max = target.getStats().get(Attribute.maxenergy);
        double current = target.getBaseStats().get(Attribute.energy);

        if (amount < 0 && current < amount) {
            // cannot update energy to negative values.
            return false;
        } else {
            if (amount + max > max) {
                // prevent updating over maximum.
                amount = max - current;
            }
            target.getBaseStats().update(Attribute.energy, amount);
            game.publish(new StatsUpdateEvent(target));
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
     * @param spell the active spell that spawned the projectile. If the spell
     *              has defined a callback for onHit then this will be called
     *              each time a projectile hits a target.
     * @return the created projectile object.
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
     * @param spellId the name of the spell to retrieve.
     * @return a spell matching the given spell name.
     */
    public Optional<Spell> getSpellById(String spellId) {
        return spells.getById(spellId);
    }


    /**
     * Checks if the given spell name is registered in the spell engine.
     *
     * @param spellName the name of the spell.
     * @return true if the spell is registered, otherwise false.
     */
    public boolean exists(String spellName) {
        return spells.getById(spellName).isPresent();
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
        Stats base = target.getBaseStats();
        Stats computed = target.getStats();
        base.update(Attribute.health, (int) value);

        // apply resistances, one percent per point.
        switch (type) {
            case physical:
                value *= 1 - (computed.getOrDefault(Attribute.armorClass, 0.0) / 100);
                break;
            case magical:
                value *= 1 - (computed.getOrDefault(Attribute.magicResist, 0.0) / 100);
                break;
        }

        game.publish(new DamageEvent(target, value, type).setSource(source));

        if (target.isDead()) {
            target.getAfflictions().clearOnDeath();
            game.movement().stop(target);
            game.publish(new DeathEvent(target, source));

            // award source with experience if player.
            if (source instanceof PlayerCreature) {
                experience(source, computed.getOrDefault(Attribute.level, 1.0).intValue() *
                        XP_PER_CREATURE_LEVEL);
            }

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
        for (Creature entity : creatures.all()) {
            AfflictionState afflictions = entity.getAfflictions();
            boolean modified = afflictions.tick(game, ticker);

            if (modified && afflictions.getStats().isDirty()) {
                game.publish(new StatsUpdateEvent(entity));
            }

            entity.getSpells().tick(entity, spells, ticker);
        }
    }

    // update progress for spells currently being casted.
    private void updateCastingProgress(Ticker ticker) {
        casting.values().removeIf((casting) -> {
            if (casting.completed(ticker)) {
                game.publish(new SpellCastEvent(casting.setCycle(SpellCycle.CASTED)));
                casting.onCastCompleted(game);

                // the spell is casted: stay active until the spell expires.
                active.add(casting);
                return true;
            } else {
                if (casting.shouldTick(ticker)) {
                    casting.onCastProgress(game);
                }
            }
            return false;
        });
    }

    // execute spell effects for spells that have been casted successfully.
    private void updateActiveSpells(Ticker ticker) {
        active.removeIf(spell -> {
            if (spell.active(ticker)) {
                if (spell.shouldTick(ticker)) {
                    spell.onSpellActive(game);
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
