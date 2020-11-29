package com.codingchili.instance.model.npc;

import com.codingchili.instance.model.entity.Model;
import com.codingchili.instance.model.stats.*;
import com.codingchili.instance.scripting.Scripted;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * NPC configuration.
 */
public class EntityConfig implements Storable {
    private List<LootTableItem> loot = new ArrayList<>();
    private String id;
    private Model model;
    private String name;
    private String description;
    private String dialog;
    private String harvest;
    private Stats stats;
    private Scripted spawn;
    private Scripted death;
    private Scripted tick;

    @Override
    public String getId() {
        return id;
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

    public List<LootTableItem> getLoot() {
        return loot;
    }

    public void setLoot(List<LootTableItem> loot) {
        this.loot = loot;
    }

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Scripted getSpawn() {
        return spawn;
    }

    public void setSpawn(Scripted spawn) {
        this.spawn = spawn;
    }

    public Scripted getDeath() {
        return death;
    }

    public void setDeath(Scripted death) {
        this.death = death;
    }

    public Scripted getTick() {
        return tick;
    }

    public void setTick(Scripted tick) {
        this.tick = tick;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String getHarvest() {
        return harvest;
    }

    public void setHarvest(String harvest) {
        this.harvest = harvest;
    }
}
