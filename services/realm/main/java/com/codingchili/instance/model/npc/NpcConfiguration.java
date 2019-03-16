package com.codingchili.instance.model.npc;

import com.codingchili.instance.model.entity.Model;
import com.codingchili.instance.model.stats.Attribute;
import com.codingchili.instance.model.stats.Stats;
import com.codingchili.instance.scripting.JexlScript;
import com.codingchili.instance.scripting.Scripted;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * NPC configuration.
 */
public class NpcConfiguration implements Storable {
    private List<LootTableItem> loot = new ArrayList<>();
    private Model model;
    private String name;
    private String description;
    private String dialog;
    private Stats stats;
    private Scripted spawn;
    private Scripted death;
    private Scripted tick;

    public static void main(String[] args) {
        NpcConfiguration npc = new NpcConfiguration();
        npc.name = "Illicit Lilith";
        npc.description = "Malevolent. Angry. Spirit.";
        npc.dialog = "tutor";
        npc.stats = new Stats()
                .set(Attribute.health, 1000)
                .set(Attribute.maxhealth, 1000);

        npc.spawn = new JexlScript("return true;");
        npc.death = new JexlScript("return true;");
        npc.tick = new JexlScript("return true;");

        npc.loot.add(new LootTableItem()
                .setItem("wooden_dagger_1")
                .setProbability(0.2f)
        );

        System.out.println(Serializer.yaml(npc));
    }

    @Override
    public String getId() {
        return name;
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
}
