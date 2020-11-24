package com.codingchili.instance.model.npc;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.instance.model.entity.Model;

public class LootableConfiguration implements Configurable {
    public static String PATH = "conf/game/looting/looting.yaml";
    private String description = "Corpse of %s, RIP";
    private String name = "Corpse";
    private Model model = new Model();
    private boolean removeWhenEmpty = false;
    private long decay = 180_000;
    private int dropDistance = 64;
    private int corpseOffsetY = -48;

    public boolean isRemoveWhenEmpty() {
        return removeWhenEmpty;
    }

    public void setRemoveWhenEmpty(boolean removeWhenEmpty) {
        this.removeWhenEmpty = removeWhenEmpty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public int getDropDistance() {
        return dropDistance;
    }

    public void setDropDistance(int dropDistance) {
        this.dropDistance = dropDistance;
    }

    public int getCorpseOffsetY() {
        return corpseOffsetY;
    }

    public void setCorpseOffsetY(int corpseOffsetY) {
        this.corpseOffsetY = corpseOffsetY;
    }

    public long getDecay() {
        return decay;
    }

    public void setDecay(long decay) {
        this.decay = decay;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
