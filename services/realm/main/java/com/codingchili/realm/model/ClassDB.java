package com.codingchili.realm.model;

import com.codingchili.common.Strings;
import com.codingchili.realm.instance.model.entity.PlayableClass;

import java.util.*;

import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 *
 * Contains a list of playable classes.
 */
public class ClassDB {
    private static Map<String, PlayableClass> classes = new HashMap<>();

    static {
        ConfigurationFactory.readDirectory(Strings.PATH_GAME_CLASSES).stream()
                .map(json -> Serializer.unpack(json, PlayableClass.class))
                .forEach(playable -> classes.put(playable.getName(), playable));
    }

    /**
     * @return a list of all PlayableClasses.
     */
    public Collection<PlayableClass> asList() {
        return classes.values();
    }

    /**
     * Retrieve a playable class by its name.
     * @param name the name of the class to retrieve.
     * @return optional with value if found, empty otherwise.
     */
    public Optional<PlayableClass> getByName(String name) {
        return Optional.ofNullable(classes.getOrDefault(name, null));
    }

    public static void main(String[] args) {
        new ClassDB().asList().forEach(playable -> {
            System.out.println(Serializer.yaml(playable));
        });
    }
}
