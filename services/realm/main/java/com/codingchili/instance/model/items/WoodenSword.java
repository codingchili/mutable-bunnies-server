package com.codingchili.instance.model.items;

import com.codingchili.instance.model.stats.Attribute;

/**
 * Sample item configured as a java object.
 */
public class WoodenSword extends Item {
    {
        slot = Slot.weapon;
        weaponType = WeaponType.sword2h;
        name = "Wooden Dagger";
        description = "Watch out for splinters.";
        rarity = ItemRarity.rare;

        stats.update(Attribute.strength, 1);
        stats.update(Attribute.attackpower, 5);
        stats.update(Attribute.attackspeed, 2);
        stats.set(Attribute.health, 1.1f);
    }
}
