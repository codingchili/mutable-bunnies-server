package com.codingchili.instance.model.npc;

/**
 * @author Robin Duda
 */
public class TileConfig {
    private TileType type = TileType.ground;
    private int width;
    private int height;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    public void applyFrom(TileConfig tile) {
        this.width = tile.getWidth();
        this.height = tile.getHeight();
    }

    public enum TileType {
        ground, water, blocking
    }
}
