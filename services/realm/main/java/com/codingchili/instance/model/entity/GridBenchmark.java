package com.codingchili.instance.model.entity;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.benchmarking.BenchmarkImplementationBuilder;

/**
 * Benchmark implementation for spatial grid/hashing.
 */
public class GridBenchmark extends BenchmarkImplementationBuilder {
    private List<Entity> entities = new ArrayList<>();
    private Grid<Entity> grid;
    private AtomicInteger index = new AtomicInteger(0);

    public GridBenchmark(String name) {
        super(name);
        //add("add", this::add);
        add("tick", this::tick);
        //add("remove", this::remove);

        // add 1k entities for default load.
        for (int i = 0; i < 1000; i++) {
            entities.add(new FakeVectorEntity());
        }
    }

    @Override
    public void reset(Handler<AsyncResult<Void>> future) {
        index.set(0);
        future.handle(Future.succeededFuture());
    }

    public GridBenchmark setGrid(Grid<Entity> grid) {
        this.grid = grid;
        entities.forEach(grid::add);
        return this;
    }

    private void add(Future<Void> future) {
        grid.add(entities.get(index()));
        future.complete();;
    }

    private int index() {
        int current = index.getAndIncrement();
        if (current >= entities.size()) {
            index.set(0);
            current = 0;
        }
        return current;
    }

    public void tick(Future<Void> future) {
        grid.update(null);
        //future.complete();
    }

    private void remove(Future<Void> future) {
        grid.add(entities.get(index()));
        future.complete();
    }

    /**
     * Supplier for a mocked vector class. The vector is unstable meaning
     * the cells it is positioned within changes with each call to #cells();
     * this simulates maximum activity and is the worst case scenario for the
     * spatial hashing algorithm.
     */
    private static class FakeVectorEntity extends PlayerCreature {
        private UnstableVector vector = new UnstableVector();

        public FakeVectorEntity() {
            super(UUID.randomUUID().toString());
        }

        @Override
        public Vector getVector() {
            return vector;
        }
    }
}
