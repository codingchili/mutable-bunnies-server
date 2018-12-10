package com.codingchili.realm.instance.model.npc;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.SimpleCreature;

/**
 * @author Robin Duda
 */
public class Npc extends SimpleCreature {

    @Override
    public void setContext(GameContext game) {
        super.setContext(game);

        // if npc/object configured with a dialog set it here.
        // we don't want the dialog configuration to be a part
        // of the game object - because that means casting.
        // this is a capability - not a field.
        game.dialogs().register(this, "tutor");
    }
}
