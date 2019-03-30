package com.codingchili.instance.model.items;

import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.scripting.ReferencedScript;
import org.apache.commons.jexl2.*;

import com.codingchili.core.protocol.Serializer;

/**
 * Sample item configured as a java object.
 */
public class WoodenSword extends Item {
    {
        slot = Slot.weapon;
        weaponType = WeaponType.sword2h;
        name = "Wooden Dagger";
        description = "Watch out for splinters.";
        rarity = ItemRarity.RARE;

        stats.update(Attribute.strength, 1);
        stats.update(Attribute.attackpower, 5);
        stats.update(Attribute.attackspeed, 2);
        stats.set(Attribute.health, 1.1f);

        onUse = new ReferencedScript("heal");
    }

    public static void main(String[] args) {
        String yaml = Serializer.yaml(new WoodenSword());
        Item item = Serializer.unyaml(yaml, Item.class);
        String yaml2 = Serializer.yaml(item);
        System.out.println(yaml2);


        JexlEngine engine = new JexlEngine();
        Script expression = engine.createScript("" +
                "stats[attribute.strength];" +
                "stats[attribute.attackpower];" +
                "return 500;");

        JexlContext context = new MapContext();
        context.set("stats", item.stats);
        context.set("attribute", Attribute.class);
        System.out.println(expression.execute(context));

    }
}
