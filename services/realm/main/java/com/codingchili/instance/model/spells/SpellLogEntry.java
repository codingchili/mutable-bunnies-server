package com.codingchili.instance.model.spells;

import java.time.Instant;

/**
 * An entry in the spell log.
 */
public class SpellLogEntry {
    private Instant created = Instant.now();
    private AttributeEvent event;

    public SpellLogEntry(AttributeEvent event) {
        this.event = event;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public AttributeEvent getEvent() {
        return event;
    }

    public void setEvent(AttributeEvent event) {
        this.event = event;
    }
}
