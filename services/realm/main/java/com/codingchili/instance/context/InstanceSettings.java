package com.codingchili.instance.context;

import com.codingchili.instance.model.SpawnPoint;
import com.codingchili.instance.model.entity.Skybox;
import com.codingchili.instance.model.entity.SpawnConfiguration;
import com.codingchili.instance.scripting.Scripted;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.configuration.Configurable;

import static com.codingchili.common.Strings.PATH_INSTANCE;
import static com.codingchili.core.configuration.CoreStrings.EXT_YAML;

/**
 * @author Robin Duda
 *
 * Contains settings for an instance in a realm.
 */
public class InstanceSettings implements Configurable {
    private List<SpawnConfiguration> structures = new ArrayList<>();
    private List<SpawnConfiguration> npcs = new ArrayList<>();
    private List<SpawnPoint> spawns = new ArrayList<>();
    private Skybox skybox = new Skybox();
    private Scripted onStartup;
    private Scripted onPlayerJoin;
    private String name = "default";
    private String texture = "";
    private int limit = 0;
    private int size = 4096;

    /**
     * @return the name of the realm.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name sets the name of the realm.
     * @return fluent
     */
    protected InstanceSettings setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the maximum number of players that may enter the instance.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the maximum number of player that may enter the instance.
     * @return fluent
     */
    protected InstanceSettings setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * @return the script executed when a player joins the instance.
     */
    public Scripted getOnPlayerJoin() {
        return onPlayerJoin;
    }

    /**
     * @param onPlayerJoin a script to execute when a player joins the instance.
     */
    public void setOnPlayerJoin(Scripted onPlayerJoin) {
        this.onPlayerJoin = onPlayerJoin;
    }

    /**
     * @return the script to run on startup.
     */
    public Scripted getOnStartup() {
        return onStartup;
    }

    /**
     * @param onStartup the script to run on startup.
     */
    public void setOnStartup(Scripted onStartup) {
        this.onStartup = onStartup;
    }

    /**
     * @return a list of configured player spawning points.
     */
    public List<SpawnPoint> getSpawns() {
        return spawns;
    }

    /**
     * @param spawns a list of player SPAWN points.
     */
    public void setSpawns(List<SpawnPoint> spawns) {
        this.spawns = spawns;
    }

    /**
     * @return the width and height of the map.
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size set the width and height of the map.
     * @return fluent
     */
    protected InstanceSettings setSize(int size) {
        this.size = size;
        return this;
    }

    /**
     * @return a list of structures to spawn on the map.
     */
    public List<SpawnConfiguration> getStructures() {
        return structures;
    }

    public void setStructures(List<SpawnConfiguration> structures) {
        this.structures = structures;
    }

    /**
     * @return a list of NPCs to spawn on the map.
     */
    public List<SpawnConfiguration> getNpcs() {
        return npcs;
    }

    public void setNpcs(List<SpawnConfiguration> npcs) {
        this.npcs = npcs;
    }

    /**
     * @return base ground texture.
     */
    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    /**
     * @return settings for the skybox.
     */
    public Skybox getSkybox() {
        return skybox;
    }

    public void setSkybox(Skybox skybox) {
        this.skybox = skybox;
    }

    @Override
    public String getPath() {
        return PATH_INSTANCE + name + EXT_YAML;
    }
}
