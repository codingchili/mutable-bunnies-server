package com.codingchili.realm.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Character creation statistics since server start.
 */
public class CreationStatistics {
    private Long since = Instant.now().toEpochMilli();
    private Map<String, Long> creations = new HashMap<>();

    public Long getSince() {
        return since;
    }

    public void setSince(Long since) {
        this.since = since;
    }

    public void created() {

    }

    public Map<String, Long> getCreations() {
        return creations;
    }

    public void setCreations(Map<String, Long> creations) {
        this.creations = creations;
    }
}
