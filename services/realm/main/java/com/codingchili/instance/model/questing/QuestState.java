package com.codingchili.instance.model.questing;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Robin Duda
 * <p>
 * Quest state attached to a PlayerCreature and stored with the character.
 */
public class QuestState {
    private Map<String, QuestProgress> progress = new LinkedHashMap<>();

    /**
     * @param questId
     * @return
     */
    public boolean has(String questId) {
        return progress.containsKey(questId);
    }

    /**
     * @param questId
     * @return
     */
    public boolean complete(String questId) {
        QuestProgress stage = progress.get(questId);
        return (stage != null) && stage.isComplete();
    }

    /**
     * @param quest
     */
    public void add(Quest quest) {
        progress.put(quest.getId(), new QuestProgress(quest));
    }

    /**
     * @param id
     * @return
     */
    public QuestProgress getById(String id) {
        return progress.get(id);
    }

    /**
     * @param id
     * @param stage
     * @return
     */
    public boolean at(String id, String stage) {
        if (has(id)) {
            QuestProgress progress = getById(id);
            return progress.getStage().equals(stage);
        } else {
            return false;
        }
    }

    /**
     * @return
     */
    public Map<String, QuestProgress> asMap() {
        return progress;
    }
}
