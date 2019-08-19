package com.codingchili.instance.model.questing;

/**
 * @author Robin Duda
 *
 * A request for quest information for the quest journal in the client.
 */
public class QuestRequest {
    private String questId;

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }
}
