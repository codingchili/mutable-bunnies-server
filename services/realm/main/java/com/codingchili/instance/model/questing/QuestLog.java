package com.codingchili.instance.model.questing;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Robin Duda
 * <p>
 * The log for a single quest, includes all completed stages, names and descriptions.
 * <p>
 * Only used for the client.
 */
public class QuestLog {
    private Collection<LogEntry> entries = new ArrayList<>();
    private String id;
    private String name;
    private String description;
    private boolean complete;

    public QuestLog(Quest quest, QuestProgress current) {
        this.id = quest.getId();
        this.name = quest.getName();
        this.description = quest.getDescription();
        this.complete = current.isComplete();

        for (QuestStage stage : quest.getStage()) {
            entries.add(new LogEntry()
                    .setName(stage.getTitle())
                    .setDescription(stage.getDescription())
                    .setId(stage.getId())
            );
            // only show events up to and including current.
            if (current.getId().equals(stage.getId())) {
                break;
            }
        }
    }


    public Collection<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(Collection<LogEntry> entries) {
        this.entries = entries;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
