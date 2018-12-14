package com.codingchili.instance.model.spells;

import com.codingchili.instance.context.GameContext;

import java.time.*;
import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Spell state that is stored on creatures, handles cooldowns and spell charges.
 */
public class SpellState {
    private static final Integer GCD_MS = 250; // todo: externalize.
    private Set<String> learned = new HashSet<>();
    private Map<String, Long> casted = new HashMap<>();
    private Map<String, Integer> charges = new HashMap<>();
    private Long gcd = 0L;

    public void setCooldown(Spell spell) {
        long now = Instant.now().toEpochMilli();
        long cooldownEndsAt = now + (1000 * spell.getCooldown().longValue());
        casted.put(spell.getId(), cooldownEndsAt);
        gcd = now + GCD_MS;

        charges.compute(spell.getId(), (id, count) -> (count == null) ? 0 : (count -= 1));
    }

    public boolean cooldown(Spell spell) {
        if (!gcd()) {
            if (charges(spell)) {
                // disregard cooldown if there are charges available.
                return false;
            } else {
                // check if the spell is on cooldown.
                Long lastCastCooldownEnds = casted.getOrDefault(spell.getId(), 0L);
                return (Instant.now().toEpochMilli() < lastCastCooldownEnds);
            }
        } else {
            // global cooldown applies to charges as well.
            return true;
        }
    }

    private boolean gcd() {
        return System.currentTimeMillis() < gcd;
    }

    private boolean charges(Spell spell) {
        if (spell.charges == 1) {
            return false; // no charges available for consumption.
        }
        charges.putIfAbsent(spell.id, 0);
        return (charges.get(spell.getId()) > 0);
    }

    public void tick(SpellDB spells, long currentTick) {
        for (String spellName : learned) {

            spells.getByName(spellName).ifPresent(spell -> {
                int cooldown = GameContext.secondsToTicks(spell.getCooldown());

                if (spell.charges > 1 && currentTick % cooldown == 0) {
                    charge(spell);
                }
            });
        }
    }

    public void charge(Spell spell) {
        charges.compute(spell.getId(), (key, charges) -> {

            if (charges == null) {
                charges = 0;
            }

            if (charges < spell.charges) {
                return charges + 1;
            } else {
                return charges;
            }
        });
    }

    public Collection<String> getLearned() {
        return learned;
    }

    public SpellState setLearned(Set<String> spellSet) {
        learned = spellSet;
        return this;
    }

    public SpellState addLearned(String spell) {
        learned.add(spell);
        return this;
    }

    public SpellState setNotLearned(String spellName) {
        learned.remove(spellName);
        return this;
    }

    public boolean learned(String spell) {
        return learned.contains(spell);
    }
}
