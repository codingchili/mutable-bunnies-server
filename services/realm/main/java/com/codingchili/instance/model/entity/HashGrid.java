package com.codingchili.instance.model.entity;


import com.codingchili.instance.context.Ticker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private ConcurrentHashMap<String, T> entities = new ConcurrentHashMap<>();
    private volatile Map<Integer, List<T>> cells = new HashMap<>();
    private int cellSize = 256;
    private int gridWidth;

    /**
     * @param width the width of the grid
     */
    public HashGrid(int width) {
        this.gridWidth = width;
    }

    @Override
    public HashGrid<T> update(Ticker ticker) {
        Map<Integer, List<T>> buffer = new HashMap<>();
        entities.values().forEach((entity) -> {

            entity.getVector().cells(cellSize).forEach(id -> {
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
        return cells.getOrDefault(col + row * gridWidth, Collections.emptyList());
    }

    @Override
    public Collection<T> all() {
        return entities.values();
    }

    @Override
    public Collection<T> translate(int x, int y) {
        return cells.getOrDefault(x / cellSize + (y / cellSize) * gridWidth, Collections.emptyList());
    }

    @Override
    public Collection<T> partition(Vector vector) {
        // entities can be partitioned into supercells for network updates.
        return entities.values();
    }

    /**
     * Degree spread for cones.
     */
    private static final Integer DEGREES = 45;

    @Override
    public Set<T> cone(Vector vector) {
        return radius(vector).stream()
                .filter(entity -> {
                    Vector other = entity.getVector();
                    double deg = Math.toDegrees(
                            Math.atan2(other.getY() - vector.getY(),
                                    other.getX() - vector.getX()));

                    deg = (deg + 360) % 360;
                    return (vector.getDirection() - DEGREES <= deg && vector.getDirection() + 45 >= deg);

                }).collect(Collectors.toSet());
    }

    @Override
    public Set<T> radius(Vector vector) {
        Set<T> results = new HashSet<>();

        vector.cells(cellSize).forEach(bucket -> {

            cells.getOrDefault(bucket, Collections.emptyList()).forEach(entity -> {
                // check the distance from the given vector to entities in adjacent buckets.
                int distance = (int) Math.hypot(
                        entity.getVector().getX() - vector.getX(),
                        entity.getVector().getY() - vector.getY());

                // consider large entities.
                int max = vector.getSize() + entity.getVector().getSize();

                if (distance < max) {
                    results.add(entity);
                }
            });
        });
        return results;
    }

    @Override
    public Set<T> adjacent(Vector vector) {
        Set<T> set = new HashSet<>();

        vector.cells(cellSize).forEach(bucket -> {
            set.addAll(cells.getOrDefault(bucket, Collections.emptyList()));
        });

        return set;
    }
}
