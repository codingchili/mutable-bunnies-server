package com.codingchili.instance.model.entity;

import com.codingchili.instance.model.npc.TileConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 *
 * Configuration used to specify where a pre-defined entity will be spawned on a map.
 */
public class SpawnConfig {
    private String id;
    private Point point;
    private Float scale;
    private String tint;
    private Boolean revertx;
    private TileConfig tile;

    public String getTint() {
        return tint;
    }

    public SpawnConfig setTint(String tint) {
        this.tint = tint;
        return this;
    }

    public Boolean getRevertx() {
        return revertx;
    }

    public void setRevertx(Boolean revertx) {
        this.revertx = revertx;
    }

    public TileConfig getTile() {
        return tile;
    }

    public void setTile(TileConfig tile) {
        this.tile = tile;
    }

    public Float getScale() {
        return scale;
    }

    public SpawnConfig setScale(Float scale) {
        this.scale = scale;
        return this;
    }

    /**
     * @return Name (id) of the entity to spawn.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id of the entity to spawn.
     */
    public SpawnConfig setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the point where the entity will be spawned.
     */
    public Point getPoint() {
        return point;
    }

    /**
     * @param point the point where the entity will be spawned.
     */
    public SpawnConfig setPoint(Point point) {
        this.point = point;
        return this;
    }

    public boolean hasScale() {
        return scale != null;
    }
}
