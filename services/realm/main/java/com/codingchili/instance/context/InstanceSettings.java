package com.codingchili.instance.context;

import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.scripting.Scripted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.configuration.Configurable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import static com.codingchili.common.Strings.PATH_INSTANCE;
import static com.codingchili.core.configuration.CoreStrings.EXT_YAML;

/**
 * @author Robin Duda
 * <p>
 * Contains settings for an instance in a realm.
 */
public class InstanceSettings implements Configurable {
    private String id;
    private String name = "default";
    private String texture = "";
    private Skybox skybox = new Skybox();
    private int limit = 0;
    private Scripted onStartup;
    private Scripted onPlayerJoin;
    private List<SpawnPoint> spawns = new ArrayList<>();
    private List<SpawnConfig> npcs = new ArrayList<>();
    private List<SpawnConfig> structures = new ArrayList<>();

    @JsonUnwrapped
    private IsometricProjection projection = new IsometricProjection();

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
     * @return the id of the instance used in scripts.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id of the instance used in scripts.
     * @return fluent
     */
    public InstanceSettings setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the isometric projection used to render the tiles.
     */
    @JsonIgnore
    public IsometricProjection getProjection() {
        return projection;
    }

    /**
     * @param projection the isometric projection used to render the tiles.
     * @return fluent
     */
    public InstanceSettings setProjection(IsometricProjection projection) {
        this.projection = projection;
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
     * @return a list of structures to spawn on the map.
     */
    public List<SpawnConfig> getStructures() {
        return structures;
    }

    public void setStructures(List<SpawnConfig> structures) {
        this.structures = structures;
    }

    /**
     * @return a list of NPCs to spawn on the map.
     */
    public List<SpawnConfig> getNpcs() {
        return npcs;
    }

    public void setNpcs(List<SpawnConfig> npcs) {
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
        return PATH_INSTANCE + id + EXT_YAML;
    }
}
