package com.codingchili.instance.model.questing;

import com.codingchili.instance.scripting.Scripted;

/**
 * @author Robin Duda
 * <p>
 * This is the configuration deserialized from disk.
 * <p>
 * Each "part" of a quest has it's own stage.
 * In the final stage, the quest is considered as completed.
 */
public class QuestStage {
    private String id;
    private String title;
    private String description;

    // called when the stage is entered.
    private Scripted enter;

    // called when the player is loaded to the game world.
    // allows the quest stage to set up listeners/tickers.
    //private Scripted initialize;

    // called when this quest stage is completed.
    private Scripted completed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Scripted getEnter() {
        return enter;
    }

    public void setEnter(Scripted enter) {
        this.enter = enter;
    }

    /*public Scripted getInitialize() {
        return initialize;
    }

    public void setInitialize(Scripted initialize) {
        this.initialize = initialize;
    }*/

    public Scripted getCompleted() {
        return completed;
    }

    public void setCompleted(Scripted completed) {
        this.completed = completed;
    }
}
