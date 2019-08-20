package com.codingchili.instance.model.questing;

/**
 * @author Robin Duda
 *
 * An entry in the quest log, these are child elements of a quest - each entry a stage.
 */
public class LogEntry {
    public String id;
    public String name;
    public String description;
    public boolean complete;

    public String getId() {
        return id;
    }

    public LogEntry setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public LogEntry setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public LogEntry setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isComplete() {
        return complete;
    }

    public LogEntry setComplete(boolean complete) {
        this.complete = complete;
        return this;
    }
}
