package com.codingchili.instance.model.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class UnstableVector extends Vector {
    private Random random = new Random();
    private Set<Integer> cells_1 = new HashSet<>();
    private Set<Integer> cells_2 = new HashSet<>();
    private AtomicBoolean use1 = new AtomicBoolean(true);

    {
        for (int i = 0; i < 4; i++) {
            cells_1.add(random.nextInt(128));
            cells_2.add(random.nextInt(128));
        }
    }

    @Override
    public Collection<Integer> cells(int cellSize) {
        /**Set<Integer> cells = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            cells.add(random.nextInt(128));
        }**/
        if (random.nextInt(10) == 2) {
            return (use1.getAndSet(!use1.get())) ? cells_1 : cells_2;
        } else {
            return use1.get() ? cells_1 : cells_2;
        }
    }
}
