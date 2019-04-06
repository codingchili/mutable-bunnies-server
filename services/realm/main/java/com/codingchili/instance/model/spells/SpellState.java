package com.codingchili.instance.model.spells;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Entity;
import com.codingchili.instance.model.events.SpellStateEvent;

import java.time.Instant;
import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Spell state that is stored on creatures, handles cooldowns and spell charges.
 * <p>
 * charges - charges can be consumed instead of waiting for a spells cooldown to pass.
 * cooldown - the amount of milliseconds that needs to pass before a spell can be cast again.
 * global cooldown - the amount of milliseconds that needs to pass between casting a spell -
 * this applies even if there are charges available for the given spell.
 * <p>
 * Unix epochs are used to set the cooldown and gcd. It is up to the caller to determine
 * if the point in time has passed. This is better than using a boolean because the delay of
 * the network does not affect the cooldown/gcd state (clients are kept in sync more accurately.)
 */
public class SpellState {
    private static final Integer GCD_MS = 250;
    private Set<String> learned = new HashSet<>();
    private Map<String, Long> casted = new HashMap<>();
    private Map<String, Float> charges = new HashMap<>();
    private Long gcd = 0L;

    /**
     * Consumes a charge for the given spell if available - otherwise puts the spell on cooldown.
     *
     * @param spell the spell to be put on cooldown.
     */
    public void setCooldown(Spell spell) {
        long now = Instant.now().toEpochMilli();
        long cooldownEndsAt = now + (1000 * spell.getCooldown().longValue());
        casted.put(spell.getId(), cooldownEndsAt);
        gcd = now + GCD_MS;

        charges.compute(spell.getId(), (id, count) -> (count == null || count == 0) ? 0 : (count -= 1));
    }

    /**
     * Checks if a spell is on cooldown - a spell is on cooldown if the global cooldown is active,
     * if there are no charges available and finally if the spell was last put on cooldown within
     * the configured cooldown for the spell.
     *
     * @param spell the spell to check if it is on cooldown.
     * @return true if the spell is on cooldown and cannot be casted.
     */
    public boolean isOnCooldown(Spell spell) {
        if (!isOnGCD()) {
            if (charges(spell) > 0) {
                // disregard cooldown if there are charges available.
                return false;
            } else {
                // check if the spell is on cooldown or out of charges if the spell is chargeable.
                Long lastCastCooldownEnds = casted.getOrDefault(spell.getId(), 0L);
                boolean cooldown = (Instant.now().toEpochMilli() < lastCastCooldownEnds);
                boolean excharged = spell.getCharges() > 0 && charges(spell) <= 0;

                return cooldown || excharged;
            }
        } else {
            // global cooldown applies to charges as well.
            return true;
        }
    }

    private boolean isOnGCD() {
        return System.currentTimeMillis() < gcd;
    }

    /**
     * @return the epoch in milliseconds of when the last activated global cooldown ends.
     * this point in time could have already passed.
     */
    public Long gcd() {
        return gcd;
    }

    /**
     * Returns the point in time when the given spells cooldown ended.
     *
     * @param spell the spell to get the cooldown for.
     * @return the epoch in milliseconds when the last cooldown of this spell ended.
     */
    public long cooldown(Spell spell) {
        return casted.getOrDefault(spell.getId(), 0L);
    }

    /**
     * Check the number of available charges for the given spell.
     *
     * @param spell the spell to check how many charges are available.
     * @return an integer indicating number of charges available.
     */
    public int charges(Spell spell) {
        charges.putIfAbsent(spell.getId(), 0f);
        return charges.get(spell.getId()).intValue();
    }

    /**
     * Processes the current cooldown and generates new charges if enough time has passed.
     *
     * @param entity the entity being updated, used for sending updates.
     * @param spells a reference to the spell database.
     * @param delta  ticker delta
     */
    public void tick(Entity entity, SpellDB spells, float delta) {
        for (String spellName : learned) {
            Optional<Spell> lookup = spells.getById(spellName);

            if (lookup.isPresent()) {
                Spell spell = lookup.get();
                int recharge = GameContext.secondsToTicks(spell.getRecharge());

                if (spell.getCharges() > 1) {
                    boolean modified = charge(spell, delta / recharge);

                    if (modified) {
                        entity.handle(new SpellStateEvent(this, spell));
                    }
                }
            }
        }
    }

    /**
     * Adds a charge for the given spell - has no effect if the owner of the spell-state
     * has not learned the spell.
     *
     * @param spell  the spell to add a charge for.
     * @param amount the amount of charge being added.
     * @return true if the amount of usable charges modified.
     */
    public boolean charge(Spell spell, float amount) {
        if (learned.contains(spell.getId())) {
            Float charge = charges.getOrDefault(spell.getId(), 0f);
            Float next = amount + charge;

            charges.put(spell.getId(), Math.min(next, spell.getCharges()));
            return (next.intValue() > charge.intValue());

        } else {
            return false;
        }
    }

    /**
     * @return a set of ID's of the spells that has been learned.
     */
    public Collection<String> getLearned() {
        return learned;
    }

    /**
     * @return a map of spell id's and the number of available charges.
     */
    public Map<String, Float> getCharges() {
        return this.charges;
    }

    /**
     * @return a map of spell id's and the unix epoch at which the cooldown expires.
     */
    public Map<String, Long> getCooldowns() {
        return this.casted;

    }

    /**
     * @param spellSet a set of spells to set as learned.
     * @return fluent.
     */
    public SpellState setLearned(Set<String> spellSet) {
        learned = spellSet;
        return this;
    }

    /**
     * @param spellId the id of the spell to add as a learned spell.
     * @return fluent.
     */
    public SpellState addLearned(String spellId) {
        learned.add(spellId);
        return this;
    }

    /**
     * @param spellId the id of the spell to unlearn.
     * @return fluent.
     */
    public SpellState setNotLearned(String spellId) {
        learned.remove(spellId);
        return this;
    }

    /**
     * @param spellId the id of a spell to check if it has been learned.
     * @return fluent.
     */
    public boolean learned(String spellId) {
        return learned.contains(spellId);
    }
}
