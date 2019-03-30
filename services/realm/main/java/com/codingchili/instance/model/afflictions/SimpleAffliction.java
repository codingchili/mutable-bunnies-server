package com.codingchili.instance.model.afflictions;

import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.JexlScript;

import com.codingchili.core.protocol.Serializer;

/**
 * Sample affliction configured as a java object.
 */
public class SimpleAffliction extends Affliction {
    {
        name = "Bleeding";
        description = "you are injured.";
        duration = 30.0f;
        tick = new JexlScript("return 1;");
        modifier = new JexlScript("return 2;");
    }

    public static void main(String[] args) {
        Affliction aff = new SimpleAffliction();
        String yaml = Serializer.yaml(aff);
        System.out.println(yaml);

        Affliction affYaml = Serializer.unyaml(yaml, Affliction.class);

        System.out.println((String) affYaml.apply(new Bindings()));
    }
}
