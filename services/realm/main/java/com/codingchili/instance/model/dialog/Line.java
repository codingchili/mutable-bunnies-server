package com.codingchili.instance.model.dialog;

/**
 * @author Robin Duda
 *
 * A line is a response from the player to a dialog from an NPC.
 */
public class Line {
    private String id;
    private String text;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
