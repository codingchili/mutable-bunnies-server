// heal the user by 24 hp.
// access to the item object!

spells.heal(source, (item.rarity.ordinal() + 1) * 16)
spells.energy(source, (item.rarity.ordinal() + 1) * 48)
source.getSpells().setGcd(500)