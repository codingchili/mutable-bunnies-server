package com.codingchili.social.model;

import java.util.Set;

/**
 * @author Robin Duda
 * <p>
 * Returned on party list.
 */
public class PartyListResponse {
    private Set<String> members;

    public PartyListResponse(Set<String> members) {
        this.members = members;
    }

    public Set<String> getMembers() {
        return members;
    }
}
