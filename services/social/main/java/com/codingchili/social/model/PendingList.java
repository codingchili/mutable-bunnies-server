package com.codingchili.social.model;

import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * A list of friend requests pending.
 */
public class PendingList {
    private Collection<String> pending = new ArrayList<>();

    public PendingList() {}

    public PendingList(Collection<String> collection) {
        this.pending = collection;
    }

    public Collection<String> getPending() {
        return pending;
    }

    public void setPending(Collection<String> pending) {
        this.pending = pending;
    }
}
