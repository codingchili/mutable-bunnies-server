package com.codingchili.instance.model.questing;

/**
 * @author Robin Duda
 *
 * Quest metadata for clients.
 */
public class QuestEntry {
    private String id;
    private String name;
    private boolean completed;

    public String getId() {
        return id;
    }

    public QuestEntry setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public QuestEntry setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isCompleted() {
        return completed;
    }

    public QuestEntry setCompleted(boolean completed) {
        this.completed = completed;
        return this;
    }
}
