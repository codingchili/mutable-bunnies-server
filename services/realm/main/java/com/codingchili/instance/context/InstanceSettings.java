package com.codingchili.instance.context;

import com.codingchili.core.configuration.Configurable;

import com.codingchili.instance.model.SpawnPoint;
import com.codingchili.instance.model.entity.Node;
import com.codingchili.instance.model.npc.Npc;
import com.codingchili.instance.model.entity.Portal;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;

import java.util.ArrayList;
import java.util.List;

import static com.codingchili.common.Strings.PATH_INSTANCE;
import static com.codingchili.core.configuration.CoreStrings.EXT_YAML;

/**
 * @author Robin Duda
 * Contains settings for an instance in a realm.
 */
public class InstanceSettings implements Configurable {
    private List<Portal> portals = new ArrayList<>();
    private List<Node> nodes = new ArrayList<>();
    private List<Npc> npc = new ArrayList<>();
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
     * @return a list of portals, exit points to other instances that exist.
     */
    public List<Portal> getPortals() {
        return portals;
    }

    /**
     * @param portals sets a list of portals that are exit points into other instances
     * @return fluent
     */
    protected InstanceSettings setPortals(List<Portal> portals) {
        this.portals = portals;
        return this;
    }

    /**
     * @param portal to add to the existing set of portals.
     * @return fluent
     */
    public InstanceSettings addPortal(Portal portal) {
        this.portals.add(portal);
        return this;
    }

    /**
     * @return a list of nodes that exists on the map.
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * @param nodes a list of nodes to set.
     * @return fluent
     */
    protected InstanceSettings setNodes(List<Node> nodes) {
        this.nodes = nodes;
        return this;
    }

    /**
     * @param node a node on the map.
     * @return fluent
     */
    public InstanceSettings addNode(Node node) {
        this.nodes.add(node);
        return this;
    }

    /**
     * @return a list of npcs on the map.
     */
    public List<Npc> getNpc() {
        return npc;
    }

    /**
     * @param npc a list of npcs to set for the map.
     * @return fluent
     */
    protected InstanceSettings setNpc(List<Npc> npc) {
        this.npc = npc;
        return this;
    }

    /**
     * @param npc adds a npc to the list of existing.
     * @return fluent
     */
    public InstanceSettings addNpc(Npc npc) {
        this.npc.add(npc);
        return this;
    }

    @Override
    public String getPath() {
        return PATH_INSTANCE + name + EXT_YAML;
    }
}
