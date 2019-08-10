// heal the user by 24 hp.
// access to the item object!

spells.heal(source, (item.rarity.ordinal() + 1) * 24)
spells.energy(source, (item.rarity.ordinal() + 1) * 16)
source.getSpells().setGcd(500)