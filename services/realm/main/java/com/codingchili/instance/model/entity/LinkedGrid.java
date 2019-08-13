package com.codingchili.instance.model.entity;

import com.codingchili.instance.context.Ticker;

import java.util.*;

/**
 * - consider width/height of entities
 * - reverse lookup for exists/remove/update
 * - linked lists for faster updates
 * <p>
 * improved vs hashmap..
 * - does not require clear on update
 * - can update single entries
 * - cheaper overall :PPP
 */
public class LinkedGrid<T extends Entity> implements Grid<T> {
    private static final int CELL_SIZE = 256;
    private List<T> all = new ArrayList<>();
    private Map<String, GridEntry<T>> reverse = new HashMap<>();
    private AreaSelector<T> selector;
    private List<List<T>> cells;
    private int width;

    /**
     * @param width of the game world in px units.
     */
    public LinkedGrid(int width) {
        this.width = width;
        this.selector = new AreaSelector<>(this, CELL_SIZE);
        int cellCount = (int) Math.pow(width * 1.0 / CELL_SIZE, 2.0);

        cells = new ArrayList<>(cellCount);

        for (int i = 0; i < cellCount; i++) {
            cells.add(new LinkedList<>());
        }
    }

    @Override
    public Collection<T> get(Integer cell) {
        if (cell >= 0 && cell < cells.size()) {
            return cells.get(cell);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Grid<T> update(Ticker ticker) {
        for (GridEntry<T> entry : reverse.values()) {
            Collection<Integer> current = cells(entry.entity);

            // check if cells changed.
            if (current != entry.cells) {
                for (Integer old : entry.cells) {
                    get(old).remove(entry.entity);
                }
                for (Integer updated : current) {
                    get(updated).add(entry.entity);
                }
                entry.cells = current;
            }
        }
        return this;
    }

    private Collection<Integer> cells(Entity entity) {
        return entity.getVector().cells(CELL_SIZE, width);
    }

    @Override
    public Grid<T> add(T entity) {
        all.add(entity);
        reverse.put(entity.getId(), new GridEntry<>(entity, cells(entity)));
        return this;
    }

    @Override
    public Grid<T> remove(String id) {
        GridEntry<T> entry = reverse.get(id);

        if (entry != null) {
            all.remove(entry.entity);

            for (Integer cell: entry.cells) {
                this.get(cell).remove(entry.entity);
            }
        }
        reverse.remove(id);
        return this;
    }

    @Override
    public T get(String id) {
        return reverse.get(id).entity;
    }

    @Override
    public boolean exists(String id) {
        return reverse.containsKey(id);
    }

    @Override
    public Collection<T> list(int col, int row) {
        int cellId = col + row * (width / CELL_SIZE);

        if (cellId > 0 && cellId < cells.size()) {
            return get(cellId);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Collection<T> all() {
        return all;
    }

    @Override
    public Collection<T> translate(int x, int y) {
        return list(x / width, y / width);
    }

    @Override
    public Collection<T> partition(Vector vector) {
        return all();
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
        vector.cells(CELL_SIZE, width).forEach(bucket -> set.addAll(get(bucket)));
        return set;
    }

    @Override
    public int width() {
        return width;
    }

    private static class GridEntry<T extends Entity> {
        private T entity;
        private Collection<Integer> cells;

        /**
         * @param entity the entity that is stored as an entry.
         */
        GridEntry(T entity, Collection<Integer> cells) {
            this.entity = entity;
            this.cells = cells;
        }

        public T entity() {
            return entity;
        }

        @Override
        public int hashCode() {
            return entity.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof GridEntry) {
                return ((GridEntry) other).entity.getId().equals(entity.getId());
            } else {
                return false;
            }
        }
    }
}
