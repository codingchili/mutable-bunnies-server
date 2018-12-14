package com.codingchili.instance.model.dialog;

/**
 * @author Robin Duda
 *
 * A line is a response from the player to a dialog from an NPC.
 */
public class Line {
    private String option;
    private String response;

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
