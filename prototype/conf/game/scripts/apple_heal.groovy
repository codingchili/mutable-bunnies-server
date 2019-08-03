// heal the user by 24 hp.
// access to the item object!

spells.heal(source, item.rarity.ordinal() * 16)
spells.energy(source, item.rarity.ordinal() * 48)
source.getSpells().setGcd(500)