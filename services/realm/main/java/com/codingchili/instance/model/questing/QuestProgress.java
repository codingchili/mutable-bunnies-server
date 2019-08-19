package com.codingchili.instance.model.questing;

/**
 * @author Robin Duda
 *
 * The current progress of a single quest, stored on player objects.
 */
public class QuestProgress {
    private String id;
    private String stage;
    private boolean complete;

    public QuestProgress() {
    }

    public QuestProgress(Quest quest) {
        this.id = quest.getId();
        this.stage = quest.getStage().get(0).getId();
        this.complete = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
