package com.codingchili.instance.model.entity;

import io.vertx.core.Future;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.codingchili.core.benchmarking.BenchmarkBuilder;
import com.codingchili.core.benchmarking.BenchmarkConsoleListener;
import com.codingchili.core.benchmarking.BenchmarkExecutor;
import com.codingchili.core.benchmarking.BenchmarkGroupBuilder;
import com.codingchili.core.benchmarking.reporting.BenchmarkHTMLReport;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.instance.context.Ticker;

/**
 * - consider width/height of entities
 * - reverse lookup for exists/remove/update
 * - linked lists for faster updates
 *
 * improved vs hashmap..
 *  - does not require clear on update
 *  - can update single entries
 *  - cheaper overall :PPP
 */
public class LinkedGrid<T extends Entity> implements Grid<T> {
    private Map<String, GridEntry<T>> reverse = new HashMap<>();
    private List<List<T>> lists;

    /**
     * @param cells
     */
    public LinkedGrid(int cells) {
        lists = new ArrayList<>(cells);

        for (int i = 0; i < cells; i++) {
            lists.add(new LinkedList<>());
        }
    }

    private List<T> cell(int cell) {
        return lists.get(cell);
    }

    public static void main(String[] args) {
        CoreContext core = new SystemContext();

        BenchmarkGroupBuilder builder =
            new BenchmarkGroupBuilder("spatial-hashing", 99_000_000_0);

        builder.add(new GridBenchmark("linked grid")
            .setGrid(new LinkedGrid<>(512)).setGroup(builder));

        Grid<Entity> g = new LinkedGrid<>(512);
        GridBenchmark gb = new GridBenchmark("linked grid")
            .setGrid(g);

        System.out.println("size=" + g.all().size());

        Future<Void> future = Future.future();
        while (true) {
            try {
                Thread.sleep(16, 0);
            } catch (InterruptedException e) {
                //
            }
            gb.tick(future);
        }

        //builder.add(new GridBenchmark("map grid")
        //  .setGrid(new HashGrid<>(512)).setGroup(builder));

        /*new BenchmarkExecutor(core)
            .setListener(new BenchmarkConsoleListener())
            .start(builder)
            .setHandler(done -> {
                new BenchmarkHTMLReport(done.result()).display();
                core.close();
            });*/
    }

    @Override
    public Grid<T> update(Ticker ticker) {
        for (GridEntry<T> g : reverse.values()) {
            Collection<Integer> hash = g.entity.getVector().cells(512);

            if (hash != g.cells) {
                // remove oldcells from buckets
                for (Integer cell : g.cells) {
                    lists.get(cell).remove(g.entity);
                }

                for (Integer cell : hash) {
                    lists.get(cell).add(g.entity);
                }

                g.cells = hash;
            }

        }
        /*reverse.forEach((id, entry) -> {
            Collection<Integer> hash = entry.entity.getVector().cells(512);

            // new memory address.
            if (hash != entry.cells) {
                // remove oldcells from buckets
                entry.cells.forEach(cell -> {
                    lists.get(cell).remove(entry.entity);
                });
                // add newcells to bucket
                hash.forEach(cell -> {
                    lists.get(cell).add(entry.entity);
                });
                // overwrite oldcells with newcells in reverse lookup.
                entry.cells = hash;
            }
        });*/
        return this;
    }

    @Override
    public Grid<T> add(T entity) {
        System.out.println("add " + entity.getId());
        reverse.put(entity.getId(), new GridEntry<T>(entity));
        return this;
    }

    @Override
    public Grid<T> remove(String id) {
        GridEntry<T> entry = reverse.get(id);
        entry.cells.forEach(cell -> {
            cell(cell).remove(entry.entity);
        });
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
        // todo translate from x,y to z.
        return cell(col + row * 512);
    }

    @Override
    public Collection<T> all() {
        return reverse.values()
            .stream()
            .map(GridEntry::entity)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<T> translate(int x, int y) {
        // todo translate from x,y to z and get cells, using vector impl?
        return new ArrayList<>();
    }

    @Override
    public Collection<T> partition(Vector vector) {
        return new ArrayList<>();
    }

    @Override
    public Set<T> cone(Vector vector) {
        return new HashSet<>();
    }

    @Override
    public Set<T> radius(Vector vector) {
        return new HashSet<>();
    }

    @Override
    public Set<T> adjacent(Vector vector) {
        return new HashSet<>();
    }

    private static class GridEntry<T extends Entity> {
        private T entity;
        private Collection<Integer> cells;

        /**
         * @param entity the entity that is stored as an entry.
         */
        public GridEntry(T entity) {
            this.entity = entity;
            this.cells = entity.getVector().cells(512);
        }

        public T entity() {
            return entity;
        }

        public Collection<Integer> cells() {
            return cells;
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
