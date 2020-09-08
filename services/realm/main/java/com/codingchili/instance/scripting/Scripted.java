package com.codingchili.instance.scripting;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Robin Duda
 * <p>
 * Interface implemented by scripting engines to provide scripting
 * support to object, entities spells and more.
 *
 * Uses a custom serializer to simplify the serialized format.
 *
 * Example:
 * {"scriptedBehavior": {"jexl": "some jexl script;"} }
 */
@JsonSerialize(using = ScriptedSerializer.class)
@JsonDeserialize(using = ScriptedDeserializer.class)
public interface Scripted {

    /**
     * @param bindings for the script execution context.
     * @param <T>      type of the scripts return value.
     * @return the return value of the script.
     */
    <T> T apply(Bindings bindings);

    /**
     * @return the name of the scripting engine that the source
     * of the script is written in.
     */
    String getEngine();

    /**
     * @return the scripts source as a text string, this is the serialized
     * form of the script.
     */
    String getSource();
}
