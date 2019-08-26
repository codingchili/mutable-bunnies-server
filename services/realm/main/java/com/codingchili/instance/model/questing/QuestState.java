package com.codingchili.instance.model.questing;

import com.codingchili.instance.model.items.CollectionFactory;

import java.util.Map;

/**
 * @author Robin Duda
 * <p>
 * Quest state attached to a PlayerCreature and stored with the character.
 */
public class QuestState {
    private Map<String, QuestProgress> progress = CollectionFactory.map();

    /**
     * Checks if the state contains the given quest id.
     *
     * @param questId the id of the quest to check.
     * @return true if the quest has been added to the state.
     */
    public boolean has(String questId) {
        return progress.containsKey(questId);
    }

    /**
     * Checks if the given quest is completed, a quest is considered complete
     * when its cursor is positioned at the final stage.
     *
     * @param questId the id of the quest to check if its completed.
     * @return true if the quest has been completed.
     */
    public boolean complete(String questId) {
        QuestProgress stage = progress.get(questId);
        return (stage != null) && stage.isComplete();
    }

    /**
     * @param quest the quest to add to the state.
     */
    public void add(Quest quest) {
        progress.put(quest.getId(), new QuestProgress(quest));
    }

    /**
     * @param id id of the quest to retrieve the progress status of.
     * @return the progress of the given quest.
     */
    public QuestProgress getById(String id) {
        return progress.get(id);
    }

    /**
     * Checks if the state includes the given quest and if the progress
     * state is positioned at the given stage.
     *
     * @param id    the id of the quest to check.
     * @param stage the id of the stage to check for.
     * @return true if the quest exist and has the matching progress stage.
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
     * @return the quest progress map.
     */
    public Map<String, QuestProgress> getProgress() {
        return progress;
    }

    /**
     * Only used for JSON serialization
     *
     * @param progress progress map
     */
    public void setProgress(Map<String, QuestProgress> progress) {
        this.progress = progress;
    }
}
