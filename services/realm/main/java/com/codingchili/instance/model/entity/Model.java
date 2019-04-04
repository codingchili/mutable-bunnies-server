package com.codingchili.instance.model.entity;

/**
 * @author Robin Duda
 * <p>
 * A graphical representation of an object to be used by the client and hit detection.
 */
public class Model {
    private String graphics = "game/placeholder.png";
    private String skin;
    private float scale = 1.0f;
    private boolean blocking = false;
    private Hitbox hitbox = new Hitbox();
    private int layer = 1;

    /**
     * @return the graphical representation of the model, a sprite for example.
     */
    public String getGraphics() {
        return graphics;
    }

    public void setGraphics(String graphics) {
        this.graphics = graphics;
    }

    /**
     * @return the scale at which the hitbox and sprite should be rendered.
     */
    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * @return indicates if the model is penetrable by a player or not.
     */
    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    /**
     * @return a list of points that create a bounding box.
     */
    public Hitbox getHitbox() {
        return hitbox;
    }

    public void setHitbox(Hitbox hitbox) {
        this.hitbox = hitbox;
    }
    
    /**
     * @return the layer at which the graphic will be rendered.
     */
    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    /**
     * @return name of the animation skin to use by default.
     */
    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }
}
