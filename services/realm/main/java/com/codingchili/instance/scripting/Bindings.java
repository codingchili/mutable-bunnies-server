package com.codingchili.instance.scripting;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.afflictions.Affliction;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Entity;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.model.stats.Stats;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robin Duda
 *
 * Bindings used when calling scripts.
 *
 * For non native scripts the map is passed as it is.
 * For native scripts this class provides typed access to binding variables.
 */
public class Bindings extends HashMap<String, Object> {
    public static final Bindings NONE = new Bindings();
    private static final String GAME = "game";
    private static final String ATTRIBUTE = "Attribute";
    private static final String AFFLICTION = "affliction";
    private static final String STATS = "stats";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String STATE = "state";

    public Bindings() {}

    public Bindings(Map<String, Object> map) {
        map.forEach(this::put);
    }

    public Bindings setContext(GameContext context) {
        put(GAME, context);
        return this;
    }

    public Bindings setAttribute(Class<Attribute> attribute) {
        put(ATTRIBUTE, attribute);
        return this;
    }

    public Bindings setAffliction(Affliction affliction) {
        put(AFFLICTION, affliction);
        return this;
    }

    public Bindings setStats(Stats stats) {
        put(STATS, stats);
        return this;
    }

    public Stats getStats() {
        return (Stats) get(STATS);
    }

    @SuppressWarnings("unchecked")
    public <T> T retrieve(String key) {
        return (T) get(key);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getState() {
        return (Map<String, Object>) get(STATE);
    }

    public GameContext getContext() {
        return (GameContext) get(GAME);
    }

    public Affliction getAffliction() {
        return (Affliction) get(AFFLICTION);
    }

    public Bindings setSource(Entity source) {
        put(SOURCE, source);
        return this;
    }

    public Bindings setTarget(Entity target) {
        put(TARGET, target);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T getSource() {
        return (T) get(SOURCE);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T getTarget() {
        return (T) get(TARGET);
    }

    public Bindings setState(Map<String,Object> state) {
        put(STATE, state);
        return this;
    }

    public Bindings set(String name, Object value) {
        put(name, value);
        return this;
    }
}
