package com.codingchili.instance.context;

import com.codingchili.instance.model.SpawnPoint;
import com.codingchili.instance.model.entity.SpawnConfiguration;
import com.codingchili.instance.scripting.Scripted;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.configuration.Configurable;

import static com.codingchili.common.Strings.PATH_INSTANCE;
import static com.codingchili.core.configuration.CoreStrings.EXT_YAML;

/**
 * @author Robin Duda
 * Contains settings for an instance in a realm.
 */
public class InstanceSettings implements Configurable {
    private List<SpawnConfiguration> entities = new ArrayList<>();
    private List<SpawnPoint> spawns = new ArrayList<>();
    private Scripted onStartup;
    private Scripted onPlayerJoin;
    private String name = "default";
    private int limit = 0;
    private int width = 1;
    private int height = 1;

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
     * @return the width of the map.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width set the width of the map.
     * @return fluent
     */
    protected InstanceSettings setWidth(int width) {
        this.width = width;
        return this;
    }

    /**
     * @return the height of the map.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height sets the height of the map.
     * @return fluent
     */
    protected InstanceSettings setHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * @return a list of npcs on the map.
     */
    public List<SpawnConfiguration> getEntities() {
        return entities;
    }

    /**
     * @param entities a list of npcs to set for the map.
     * @return fluent
     */
    protected InstanceSettings setEntities(List<SpawnConfiguration> entities) {
        this.entities = entities;
        return this;
    }

    /**
     * @param npc adds a entities to the list of existing.
     * @return fluent
     */
    public InstanceSettings addNpc(SpawnConfiguration npc) {
        this.entities.add(npc);
        return this;
    }

    @Override
    public String getPath() {
        return PATH_INSTANCE + name + EXT_YAML;
    }
}
