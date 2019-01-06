package com.codingchili.instance.model.npc;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.Ticker;
import com.codingchili.instance.model.entity.SimpleCreature;
import com.codingchili.instance.model.stats.Attribute;

/**
 * @author Robin Duda
 */
public class TalkingPerson extends SimpleCreature {
    private GameContext game;

    {
        vector.setX(425);
        vector.setY(275);
    }

    @Override
    public void setContext(GameContext game) {
        this.game = game;
        super.setContext(game);
        game.ticker(this::tick, GameContext.secondsToTicks(8));

        game.dialogs().register(this, "tutor");
    }


    // todo: how do we know dialogs is supported on this entity?
    // - register on 'dialog' route -> client checks interactions?
    // todo: how do we know which dialog to use?
    // - we don't - the dialog handler on the npc knows this.
    // - the dialog handler on the NPC starts the interaction,
    // 1. client notices interaction available
    // 2. client sends dialog request to engine
    // 3. engine sends event to entity
    // 4. entity has registered a dialog behavior handler
    // 5. the dialog behaviour handler reads dialog config + starts the dialog
    // 6. from here on the dialog is handled by the engine
    // todo: emit interactions
    // todo: respond to dialog interaction by starting dialog

    public void tick(Ticker ticker) {
        if (stats.get(Attribute.health) > 0) {
            game.dialogs().say(getId(), "hello guys.");
        } else {
            ticker.disable();
        }
    }
}
