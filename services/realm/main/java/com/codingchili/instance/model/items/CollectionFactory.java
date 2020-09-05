package com.codingchili.instance.model.items;

import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Used to get implementations for different types of collections. The implementations
 * provided by this factory are ensured to work with the storage and serialization
 * models that the storage and game loop depends on.
 */
public class CollectionFactory {

    /**
     * @param <K> key type of the map.
     * @param <V> value type of the map.
     * @return a map implementation for the given generic type arguments.
     */
    public static <K, V> Map<K, V> map() {
        return new LinkedHashMap<>();
    }

    /**
     * @param <V> value type of the set.
     * @return a set implementation for the given generic type argument.
     */
    public static <V> Set<V> set() {
        return new LinkedHashSet<>();
    }

    /**
     * @param <V> value type of the list.
     * @return a list implementation for the given generic type argument.
     */
    public static <V> List<V> list() {
        return new ArrayList<>(32);
    }
}
