package com.codingchili.instance.model.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Robin Duda
 * <p>
 * Utility methods for performing AOE selections over a Grid.
 */
public class AreaSelector<T extends Entity> {
    private Grid<T> grid;
    private int cellSize;

    public AreaSelector(Grid<T> grid, int cellSize) {
        this.cellSize = cellSize;
        this.grid = grid;
    }

    /**
     * Degree spread for cones.
     */
    private static final Integer DEGREES = 45;

    /**
     * Finds all entities in a cone from the given vectors position, direction and size.
     * The cone is 45 degrees wide.
     *
     * @param vector the starting vector for target selection
     * @return a list of entities of type T matching the query vector.
     */
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

    /**
     * Finds all entities in radius of the vector given its position and size.
     *
     * @param vector the starting vector for target selection
     * @return a list of entities of type T matching the query vector.
     */
    public Set<T> radius(Vector vector) {
        Set<T> results = new HashSet<>();

        vector.cells(cellSize, grid.width()).forEach(bucket -> {

            grid.get(bucket).forEach(entity -> {
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

}
