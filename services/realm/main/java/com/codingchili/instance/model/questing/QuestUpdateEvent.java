package com.codingchili.instance.model.questing;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

/**
 * @author Robin Duda
 *
 * Emitted when a quest log has been updated.
 */
public class QuestUpdateEvent implements Event {
    private String id;
    private String name;

    public QuestUpdateEvent(Quest quest) {
        this.id = quest.getId();
        this.name = quest.getName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public EventType getRoute() {
        return EventType.quest_update;
    }
}
