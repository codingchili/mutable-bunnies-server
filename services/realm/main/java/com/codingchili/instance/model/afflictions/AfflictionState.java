package com.codingchili.instance.model.afflictions;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.Ticker;
import com.codingchili.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * @author Robin Duda
 * <p>
 * The affliction state contains applied afflictions to a single creature.
 */
public class AfflictionState extends ConcurrentLinkedQueue<ActiveAffliction> {
    private transient Stats stats = new Stats();

    @JsonIgnore
    public Stats getStats() {
        return stats;
    }

    @JsonIgnore
    public void setStats(Stats modifiers) {
        this.stats = modifiers;
    }

    /**
     * adds a new affliction.
     *
     * @param affliction the affliction to add.
     * @param game       the game context to send updates to.
     */
    public void add(ActiveAffliction affliction, GameContext game) {
        add(affliction);
        update(game);
    }

    /**
     * Checks if an affliction is added by its name.
     *
     * @param afflictionName the name of the affliction.
     * @return true if the affliction is active in this state.
     */
    public boolean has(String afflictionName) {
        for (ActiveAffliction active : this) {
            if (active.getAffliction().getName().equals(afflictionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes afflictions based on a predicate.
     *
     * @param predicate the predicate to determine if an affliction is to be removed.
     * @param game      the game context to send updates to.
     */
    public void removeIf(Predicate<ActiveAffliction> predicate, GameContext game) {
        AtomicBoolean modified = new AtomicBoolean(false);
        removeIf((affliction) -> {
            if (predicate.test(affliction)) {
                modified.set(true);
                return true;
            }
            return false;
        });
        if (modified.get()) {
            update(game);
        }
    }

    /**
     * Updates the state of afflictions and removes those that have expired.
     *
     * @param game   a reference to the game context for sending updates on change.
     * @param ticker the delta time to consider on update operations.
     * @return true if an afflication has been removed.
     */
    public boolean tick(GameContext game, Ticker ticker) {
        AtomicBoolean modified = new AtomicBoolean(false);
        removeIf(active -> {
            if (active.shouldTick(ticker)) {
                boolean remove = !active.tick(game);

                if (remove) {
                    modified.set(true);
                }
                return remove;
            }
            return false;
        }, game);
        return modified.get();
    }

    private void update(GameContext game) {
        stats.clear();
        forEach(active ->
                stats = stats.apply(active.modify(game)));
    }

    public void clearOnDeath() {
        removeIf(active -> !active.getAffliction().isPersist());
    }
}
