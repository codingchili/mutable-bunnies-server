package com.codingchili.instance.model.entity;

import java.util.Collection;
import java.util.Set;

import com.codingchili.instance.context.Ticker;

/**
 * Spatial hashing and querying of the game world.
 *
 * @param <T> the entity stored in the grid.
 */
public interface Grid<T extends Entity> {

    /**
     * Updates the entities in the grid.
     *
     * @param ticker the ticker that triggered the update.
     * @return fluent.
     */
    Grid<T> update(Ticker ticker);

    /**
     * @param entity the entity to add to the grid.
     * @return fluent.
     */
    Grid<T> add(T entity);

    /**
     * @param id the id of the entity to remove.
     * @return fluent.
     */
    Grid<T> remove(String id);

    /**
     * @param id the id of the entity to retrieve.
     * @return an entity with the given id.
     */
    T get(String id);

    /**
     * @param id the id to check if there is an entity registered for.
     * @return true if the entity exists on the grid.
     */
    boolean exists(String id);

    /**
     * @param col the column to retrieve entities from.
     * @param row the row to retrieve entities from.
     * @return a list of entities in the given cell.
     */
    Collection<T> list(int col, int row);

    /**
     * @return all entities in the grid.
     */
    Collection<T> all();


    /**
     * @param x the x position to get the cell of
     * @param y the y position to get the cell of
     * @return a point in the world converted to a cell.
     */
    Collection<T> translate(int x, int y);

    /**
     * @param vector a vector that exists within a network partition.
     * @return a list of entities that exists in the same network partition.
     */
    Collection<T> partition(Vector vector);

    /**
     * @param vector contains a position which is the center of the cone and a direction which
     *               points the cone, vectors size is the radius length.
     * @return entities that exists within the cone.
     */
    Set<T> cone(Vector vector);

    /**
     * @param vector contains the base position from which to expand a radius.
     *               The size of the vector is mapped to the length of the radius.
     * @return entities that exists within the given radius.
     */
    Set<T> radius(Vector vector);

    /**
     * @param vector a vector that exists in the grid, adjacent entities are selected.
     * @return adjacent entities to the given vector.
     */
    Set<T> adjacent(Vector vector);
}
