package com.codingchili.instance.model.dialog;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robin Duda
 *
 * Representation of a dialog tree.
 *
 * The dialog tree has an id for referencing from an NPC.
 * A starting point that points to one of the options.
 * For each option an option references, a response is available for the player -
 * if the filter of a referenced option allows it.
 */
public class Dialog {
    private Map<String, Option> options = new HashMap<>();
    private String id;
    private String start;

    public Map<String, Option> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Option> options) {
        this.options = options;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Option get(String optionKey) {
        return options.get(optionKey);
    }
}
