package com.codingchili.instance.model.questing;

import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

/**
 * @author Robin Duda
 * <p>
 * This event is emitted whenever a quest is added to the player.
 */
public class QuestAddEvent implements Event {
    private PlayerCreature player;
    private Quest quest;

    public QuestAddEvent(PlayerCreature player, Quest quest) {
        this.quest = quest;
        this.player = player;
    }

    public String getName() {
        return quest.getName();
    }

    public String getDescription() {
        return quest.getDescription();
    }

    public QuestProgress getStage() {
        return player.getQuests().getById(quest.getId());
    }

    @Override
    public EventType getRoute() {
        return EventType.quest_accepted;
    }
}
