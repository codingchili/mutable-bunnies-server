import com.codingchili.instance.model.entity.PlayerCreature

creatures = game.creatures().radius(
        source.getVector().copy().setSize(512)
)

// get creatures in range that does not have had the dialog triggered by this npc.
creatures.each {
    if (!state.containsKey(it.id) && it instanceof PlayerCreature) {
        game.dialogs().trigger(source.id, it.id, "gargazar")
        state.put(it.id, true)
    } else {
        // already in state - ignore npc.
    }
}