package com.codingchili.instance.model;

import io.vertx.core.buffer.Buffer;

import java.util.Map;
import java.util.Optional;

/**
 * @author Robin Duda
 * <p>
 * Describes a database that contains game information.
 */
public interface MetadataStore<T> {

    /**
     * Retrieves an item from the store given its name.
     *
     * @param id unique id to retrieve a specific item.
     * @return an item if one exists, empty otherwise.
     */
    Optional<T> getById(String id);

    /**
     * Retrieve the database as a map. Good for local operations.
     * Recommended to use {@link #toBuffer()} when sending to clients.
     *
     * @return a map of all available items in the metadata store.
     */
    Map<String, T> asMap();

    /**
     * Forces a refresh of the cached serialized object.
     * Will only be reloaded when data is loaded from disk on change or startup.
     */
    void evict();

    /**
     * @return a cached serialization of all items in the metadata store,
     * this can be written to the client directly.
     */
    Buffer toBuffer();
}
