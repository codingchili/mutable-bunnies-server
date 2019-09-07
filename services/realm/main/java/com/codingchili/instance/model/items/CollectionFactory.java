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
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> map() {
        return new LinkedHashMap<>();
    }

    /**
     * @param <V>
     * @return
     */
    public static <V> Set<V> set() {
        return new LinkedHashSet<>();
    }

    /**
     * @param <V>
     * @return
     */
    public static <V> List<V> list() {
        return new ArrayList<>(32);
    }
}
