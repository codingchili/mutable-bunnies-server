package com.codingchili.instance.model.npc;

import com.codingchili.instance.model.entity.SimpleCreature;
import com.codingchili.instance.model.events.ChatEvent;
import com.codingchili.instance.model.stats.Attribute;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 */
public class ListeningPerson extends SimpleCreature {
    public static int called = 0;

    {
        vector.setX(300);
        vector.setY(200);
    }

    public ListeningPerson() {
        super();
        // to be loaded from template.
        baseStats.set(Attribute.maxhealth, 200);
        baseStats.set(Attribute.health, 200);
        baseStats.set(Attribute.level, 1);
        baseStats.set(Attribute.constitution, 5);
        baseStats.set(Attribute.dexterity, 5);
    }

    // npcs/structures can listen for events.

    @Api(route = "chat")
    public void chatevent(ChatEvent event) {
        //   ChatEvent CHAT = ChatEvent.class.cast(event);
        //     System.out.println(CHAT.getText());
        called += 1;
        System.out.println(event.getText());

        if (!event.getSource().equals(getId())) {
            game.dialogs().say(getId(), "hi there " + game.getById(event.getSource()).getName());
        }
    }
}
