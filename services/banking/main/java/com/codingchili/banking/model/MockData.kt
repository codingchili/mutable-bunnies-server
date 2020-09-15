package com.codingchili.banking.model

import java.util.*
import kotlin.random.Random

/**
 * Provides mock data for the local mock implementations.
 */
class MockData {
    companion object {
        const val delay = 950L

        fun stats(rarity: ItemRarity): Stats {
            val attributes = setOf(
                "dexterity",
                "intelligence",
                "constitution",
                "strength",
                "armor",
                "spellpower",
                "energy",
                "movement",
                "wisdom"
            )
            val stats = Stats()

            // generate a few random attributes, additional attributes based on rarity.
            for (x in 0..Random.nextInt(4) + rarity.ordinal) {
                // generate attribute values based on the given item rarity.
                val value = ((Random.nextInt(8) - 2)) * (rarity.ordinal + 1)
                // don't show +0 on attributes, but still support penalties.
                if (value != 0) {
                    stats[attributes.random()] = value
                }
            }
            return stats
        }

        val spacewand = Item(
            icon = "wand_1.png",
            rarity = ItemRarity.rare,
            quantity = 1,
            name = "Spacewand +2",
            slot = Type.weapon,
            type = "staff",
            stats = stats(ItemRarity.rare),
            description = "Rumors have it that the spacewand is actually from space. Maybe there's actually alien wizards out there."
        )

        val greenApple = Item(
            icon = "apple_green.png",
            rarity = ItemRarity.rare,
            quantity = 99,
            name = "Green Apple",
            type = Type.consumable,
            description = "Its just a green apple. A deliciously green, fresh and juicy apple. MMmmmmm."
        )

        val flamingStick = Item(
            icon = "flaming_stick.png",
            rarity = ItemRarity.legendary,
            quantity = 1,
            name = "Flaming Stick +4",
            slot = Type.weapon,
            type = "staff",
            stats = stats(ItemRarity.legendary),
            description = "Its a stick. With fire. A Flaming stick. Slightly more dangerous than a regular stick. It is said that the flame lasts forever and clings onto anything that touches it."
        )

        val sauring = Item(
            icon = "ring_1.png",
            rarity = ItemRarity.mythic,
            quantity = 1,
            name = "The Sauring",
            slot = "ring",
            type = Type.armor,
            stats = stats(ItemRarity.mythic),
            description = "The mythic ring that was once worn by Sauron himself. What the ring does is left a mystery, until it sits firmly on your finger."
        )

        val branch = Item(
            icon = "branch.png",
            rarity = ItemRarity.common,
            quantity = 1,
            name = "Leafy Branch +1",
            slot = "weapon",
            type = "staff",
            stats = stats(ItemRarity.common),
            description = "Its just a regular branch. Without the fire. Green leaves, wow wow."
        )

        fun randomItem(): Item {
            val item = listOf(
                    branch,
                    sauring,
                    spacewand,
                    flamingStick,
                    greenApple
            ).random()

            // make sure to use unique ids here.
            item.id = UUID.randomUUID().toString()

            return item
        }
    }
}