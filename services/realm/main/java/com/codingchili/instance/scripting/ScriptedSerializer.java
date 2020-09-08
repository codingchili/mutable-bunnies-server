package com.codingchili.instance.scripting;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * @author Robin Duda
 * <p>
 * Custom jackson serializer to pack engine name -> script value pairs.
 */
public class ScriptedSerializer extends StdSerializer<Scripted> {

    public ScriptedSerializer() {
        super(Scripted.class);
    }

    @Override
    public void serialize(Scripted value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeFieldName(value.getEngine());
        gen.writeString(value.getSource());
        gen.writeEndObject();
    }
}
