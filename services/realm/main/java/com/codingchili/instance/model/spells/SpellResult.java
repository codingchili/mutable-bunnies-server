package com.codingchili.instance.model.spells;

/**
 * @author Robin Duda
 * <p>
 * When requesting to cast a spell a SpellResult is returned.
 */
public enum SpellResult {
    CASTING,        // spell is now casting.
    COOLDOWN,       // spell is on cooldown.
    UNABLE,         // script rejected the request - no energy? etc.
    UNKNOWN_SPELL   // the caster does not know the spell.
}
