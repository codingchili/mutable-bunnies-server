package com.codingchili.instance.model.entity;


import com.codingchili.instance.context.Ticker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robin Duda
 * <p>
 * A grid to map entities onto a spatially hashed map.
 *
 * Backed by hashmaps which introduce a lot of garbage on each
 * update. Additionally there's no reverse lookups so the grid
 * is cleared on every tick.
 */
public class HashGrid<T extends Entity>  implements Grid<T> {
    private static final int CELL_SIZE = 256;
    private ConcurrentHashMap<String, T> entities = new ConcurrentHashMap<>();
    private volatile Map<Integer, List<T>> cells = new HashMap<>();
    private AreaSelector<T> selector;
    private int width;

    /**
     * @param size the size of the grid in px of the longest axis.
     */
    public HashGrid(int size) {
        this.width = size;
        this.selector = new AreaSelector<>(this, CELL_SIZE);
    }

    @Override
    public HashGrid<T> update(Ticker ticker) {
        Map<Integer, List<T>> buffer = new HashMap<>();
        entities.values().forEach((entity) -> {
            entity.getVector().cells(CELL_SIZE, width).forEach(id -> {
                List<T> list = buffer.computeIfAbsent(id, key -> new ArrayList<>());
                list.add(entity);
            });
        });
        Map<Integer, List<T>> tmp = cells;
        cells = buffer;
        tmp.clear();
        return this;
    }

    @Override
    public HashGrid<T> add(T entity) {
        entities.put(entity.getId(), entity);
        return this;
    }

    @Override
    public HashGrid<T> remove(String id) {
        entities.remove(id);
        return this;
    }

    @Override
    public Collection<T> get(Integer cell) {
        return cells.get(cell);
    }

    @Override
    public T get(String id) {
        T entity = entities.get(id);
        Objects.requireNonNull(entity, String.format("No entity with id '%s' found.", id));
        return entities.get(id);
    }

    @Override
    public boolean exists(String id) {
        return entities.containsKey(id);
    }

    @Override
    public Collection<T> list(int col, int row) {
        return cells.getOrDefault(col + row * width, Collections.emptyList());
    }

    @Override
    public Collection<T> all() {
        return entities.values();
    }

    @Override
    public Collection<T> translate(int x, int y) {
        return cells.getOrDefault(x / CELL_SIZE + (y / CELL_SIZE) * width, Collections.emptyList());
    }

    @Override
    public Collection<T> partition(Vector vector) {
        // entities can be partitioned into supercells for network updates.
        return entities.values();
    }

    @Override
    public Set<T> cone(Vector vector) {
        return selector.cone(vector);
    }

    @Override
    public Set<T> radius(Vector vector) {
        return selector.radius(vector);
    }

    @Override
    public Set<T> adjacent(Vector vector) {
        Set<T> set = new HashSet<>();

        vector.cells(CELL_SIZE, width).forEach(bucket -> {
            set.addAll(cells.getOrDefault(bucket, Collections.emptyList()));
        });

        return set;
    }

    @Override
    public int width() {
        return width;
    }
}