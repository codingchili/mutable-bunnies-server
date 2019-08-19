package com.codingchili.instance.model.questing;

import java.util.*;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * A quest object as defined in configuration.
 */
public class Quest implements Storable {
    private List<QuestStage> stage = new ArrayList<>();
    public String id;
    public String name;
    public String description;

    public List<QuestStage> getStage() {
        return stage;
    }

    public void setStage(List<QuestStage> stage) {
        this.stage = stage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public QuestStage getFirstStage() {
        return stage.get(0);
    }

    public Optional<QuestStage> getStageById(String id) {
        return stage.stream()
                .filter(stage -> stage.getId().equals(id))
                .findFirst();
    }

    public boolean isFinalStage(String id) {
        return stage.get(stage.size() - 1).getId().equals(id);
    }

    public Optional<QuestStage> getNextStage(String stageId) {
        for (int i = 0; i < stage.size() - 1; i++) {
            if (stage.get(i).getId().equals(stageId)) {
                return Optional.of(this.stage.get(i + 1));
            }
        }
        return Optional.empty();
    }
}
