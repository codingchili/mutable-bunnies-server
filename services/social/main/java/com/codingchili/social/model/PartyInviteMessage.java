package com.codingchili.social.model;

import com.codingchili.common.ReceivableMessage;

/**
 * @author Robin Duda
 *
 * Contains an invitation to join a party.
 */
public class PartyInviteMessage implements ReceivableMessage {
    private String from;
    private String to;
    private String id;

    public PartyInviteMessage(String from, String to, String id) {
        this.from = from;
        this.to = to;
        this.id = id;
    }

    public String getPartyId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    @Override
    public String target() {
        return to;
    }

    @Override
    public String route() {
        return "party_invite";
    }
}
