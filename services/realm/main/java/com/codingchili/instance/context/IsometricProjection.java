package com.codingchili.instance.context;

import com.codingchili.instance.model.entity.Point;

/**
 * @author Robin Duda
 * <p>
 * Contains information about the isometric projection and tile size.
 */
public class IsometricProjection {
    private Point size = new Point(32, 32);
    private int tileSizePx = 96;

    /**
     * @return the width and height of the map in tiles.
     */
    public Point getSize() {
        return size;
    }

    /**
     * @param size set the width and height of the map in tiles.
     * @return fluent
     */
    public IsometricProjection setSize(Point size) {
        this.size = size;
        return this;
    }

    /**
     * @return size of the tile in pixels before any rotation/scaling is applied for isometric perspective.
     */
    public int getTileSizePx() {
        return tileSizePx;
    }

    /**
     * @param tileSizePx the tile width and height in px before any transformation is applied.
     */
    public void setTileSizePx(int tileSizePx) {
        this.tileSizePx = tileSizePx;
    }

    /**
     * @return the width of a single tile after applying isometric transformation.
     */
    public double getTileScaledWidth() {
        return Math.sqrt(Math.pow(tileSizePx, 2) + Math.pow(tileSizePx, 2));
    }

    /**
     * @return the height of a single tile after applying isometric transformation.
     */
    public double getTileScaledHeight() {
        return (getTileScaledWidth() * Math.tan(Math.PI / 6));
    }

    /**
     * @return the cartesian coordinate of the most northern point in the isometric grid.
     */
    public Point getNorthPoint() {
        return new Point((int) (size.getX() * getTileScaledWidth() / 2), 0);
    }

    /**
     * @return the cartesian coordinate of the most southern point in the isometric grid.
     */
    public Point getSouthPoint() {
        return new Point(
                (int) (getTileScaledWidth() * size.getY() * 0.5),
                (int) ((getTileScaledHeight() * size.getX() + getTileScaledHeight() * size.getY()) / 2));
    }

    /**
     * @return the size of the cartesian coordinate system which contains the isometric projection.
     * This is the Y point of the southern point and the easternmost X point of the isometric projection.
     */
    public Point getCartesianSize() {
        return new Point((int) (size.getX() * getTileScaledWidth() * 0.5), getSouthPoint().getY());
    }
}
