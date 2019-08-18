package com.codingchili.instance.model.dialog;

import com.codingchili.instance.model.entity.Entity;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;

import java.util.*;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * Representation of a dialog tree.
 * <p>
 * The dialog tree has an id for referencing from an NPC.
 * A starting point that points to one of the options.
 * For each option an option references, a response is available for the player -
 * if the filter of a referenced option allows it.
 */
public class Dialog implements Storable {
    private Map<String, Option> options = new LinkedHashMap<>();
    private Scripted enabled;
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

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Option get(String optionKey) {
        return options.get(optionKey);
    }

    public Scripted getEnabled() {
        return enabled;
    }

    public void setEnabled(Scripted enabled) {
        this.enabled = enabled;
    }

    public boolean enabled(Entity source, Entity target) {
        if (enabled != null) {
            Boolean is = enabled.apply(
                    new Bindings()
                            .setSource(source)
                            .setTarget(target)
            );
            Objects.requireNonNull(is, "enabled check for dialog " + id + " returned 'null'.");
            return is;
        } else {
            return true;
        }
    }
}
