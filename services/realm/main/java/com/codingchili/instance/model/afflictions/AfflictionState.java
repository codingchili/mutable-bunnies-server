package com.codingchili.instance.model.afflictions;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * @author Robin Duda
 *
 * The affliction state contains applied afflictions to a single creature.
 */
public class AfflictionState {
    private Stats stats = new Stats();
    private Queue<ActiveAffliction> list = new ConcurrentLinkedQueue<>();

    @JsonIgnore
    public Stats getStats() {
        return stats;
    }

    @JsonIgnore
    public void setStats(Stats modifiers) {
        this.stats = modifiers;
    }

    /**
     * @return a list of afflictions that are active.
     */
    public Queue<ActiveAffliction> getList() {
        return list;
    }

    /**
     * @param list a list of afflictions to set.
     */
    public void setList(Queue<ActiveAffliction> list) {
        this.list = list;
    }

    /**
     * adds a new affliction.
     * @param affliction the affliction to add.
     * @param game the game context to send updates to.
     */
    public void add(ActiveAffliction affliction, GameContext game) {
        list.add(affliction);
        update(game);
    }

    /**
     * Checks if an affliction is added by its name.
     * @param afflictionName the name of the affliction.
     * @return true if the affliction is active in this state.
     */
    public boolean has(String afflictionName) {
        for (ActiveAffliction active : list) {
            if (active.getAffliction().getName().equals(afflictionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes afflictions based on a predicate.
     * @param predicate the predicate to determine if an affliction is to be removed.
     * @param game the game context to send updates to.
     */
    public void removeIf(Predicate<ActiveAffliction> predicate, GameContext game) {
        list.removeIf((affliction) -> {
            if (predicate.test(affliction)) {
                update(game);
                return true;
            }
            return false;
        });
    }

    public void update(GameContext game) {
        stats.clear();
        list.forEach(active ->
                stats = stats.apply(active.modify(game)));
    }
}
