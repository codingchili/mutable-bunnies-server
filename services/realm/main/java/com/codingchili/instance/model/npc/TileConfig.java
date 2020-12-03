package com.codingchili.instance.model.npc;

/**
 * @author Robin Duda
 */
public class TileConfig {
    private TileType type = TileType.ground;

    public enum TileType {
        ground, water, blocking
    }
}
