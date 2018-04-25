package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.context.Ticker;
import com.codingchili.realm.instance.model.afflictions.ActiveAffliction;
import com.codingchili.realm.instance.model.afflictions.AfflictionDB;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.entity.Grid;
import com.codingchili.realm.instance.model.events.*;
import com.codingchili.realm.instance.model.stats.Attribute;

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
    private Collection<ActiveSpell> passive = new ConcurrentLinkedQueue<>();
    private Collection<Projectile> projectiles = new ConcurrentLinkedQueue<>();
    private Grid<Creature> creatures;
    private AfflictionDB afflictions;
    private SpellDB spells;
    private GameContext game;
    private Integer tick = 0;

    /**
     * Creates a new spell engine on the given game context.
     *
     * @param game the game context the spell engine is to be attached to.
     */
    public SpellEngine(GameContext game) {
        this.game = game;
        this.creatures = game.creatures();
        this.spells = new SpellDB(game.getInstance());
        this.afflictions = new AfflictionDB(game.getInstance());

        game.ticker(this::tick, 1);
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
                if (caster.getSpells().cooldown(spell.get())) {
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
                        affliction.getAffliction().getName().matches(regex),
                game);
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
     */
    public void energy(Creature target, int amount) {
        target.getStats().update(Attribute.energy, amount);
    }

    /**
     * Heals the given target by applying more health. Targets may not be healed
     * above the maximum health.
     *
     * @param target the target to apply the health to.
     * @param value  the amount of health to apply, may not exceed the max health of the being.
     */
    public void heal(Creature target, double value) {
        float max = target.getBaseStats().get(Attribute.maxhealth);
        float current = target.getBaseStats().get(Attribute.health);
        float next = (float) Math.min(max, current + value);

        target.getBaseStats().set(Attribute.health, next);

        game.publish(new DamageEvent(target, value, DamageType.heal));
    }

    /**
     * @param spell      the active spell that spawned the projectile. If the spell
     *                   has defined a callback for onHit then this will be called
     *                   each time a projectile hits a target.
     * @param properties properties of the projectiles to create.
     */
    public void projectile(ActiveSpell spell, Map<String, Float> properties) {
        projectiles.add(new Projectile(game, spell)); // todo set properties.
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
            // todo: if the player is already dead: dont kill it AGAIN.
            // prevent healing/damaging dead targets, remove afflictions etc.v
            game.publish(new DeathEvent(target, source));
        }
    }

    // calls update methods.
    private void tick(Ticker ticker) {
        updateCreatureSpellState();
        updateCastingProgress();
        updateActiveSpells();
        updateProjectiles();

        if (tick == Integer.MAX_VALUE) {
            tick = 0;
        }
        tick++;
    }

    // update affliction state and spell cooldowns.
    private void updateCreatureSpellState() {
        creatures.all().forEach(entity -> {
            entity.getAfflictions().removeIf(active -> {
                if (active.shouldTick(active.getStart() + tick))
                    if (!active.tick(game)) return true;
                return false;
            }, game);
            entity.getSpells().tick(spells, tick);
        });
    }

    // update progress for spells currently being casted.
    private void updateCastingProgress() {
        casting.values().removeIf((casting) -> {
            if (casting.completed()) {
                game.publish(new SpellCastEvent(casting.setCycle(SpellCycle.CASTED)));
                casting.onCastCompleted(game);

                // the spell is casted: stay active until the spell expires.
                passive.add(casting);
                return true;
            } else {
                if (casting.shouldTick(tick)) {
                    casting.onCastProgress(game, tick);
                }
            }
            return false;
        });
    }

    // execute spell effects for spells that have been casted successfully.

    private void updateActiveSpells() {
        passive.removeIf(spell -> {
            if (spell.active()) {

                if (spell.shouldTick(tick)) {
                    spell.onSpellEffects(game, tick);
                }

                return false;
            } else {
                return true;
            }
        });
    }
    // updates all projectiles.

    private void updateProjectiles() {
        projectiles.removeIf(Projectile::tick);
    }
}
