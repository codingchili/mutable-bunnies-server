package com.codingchili.instance.model.items;

import java.util.Arrays;
import java.util.List;

/**
 * @author Robin Duda
 */
public class Apple extends Item {
    private static List<AppleType> types = Arrays.asList(AppleType.RED, AppleType.GREEN, AppleType.GOLDEN);
    private AppleType type;

    {
        type = types.get((int) (Math.random() * types.size()));

        slot = Slot.none;
        name = type.name().toLowerCase() + " Apple";
        description = "Shiny and juicy, heals some health.";
        icon = type.getIcon();
        quantity = (int) (Math.random() * 16);
        rarity = type.rarity;
        onUse = "apple_heal.groovy";

        setId(Apple.class.getSimpleName() + "." + type.rarity);
    }

    private enum AppleType {
        RED(ItemRarity.common), GREEN(ItemRarity.uncommon), GOLDEN(ItemRarity.legendary);

        private ItemRarity rarity;

        public String getIcon() {
            return "apple_" + this.name().toLowerCase() + ".png";
        }

        public ItemRarity getRarity() {
            return rarity;
        }

        AppleType(ItemRarity rarity) {
            this.rarity = rarity;
        }
    }
}
