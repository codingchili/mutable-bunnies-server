package com.codingchili.social.model;

import com.codingchili.common.ReceivableMessage;

/**
 * @author Robin Duda
 * <p>
 * The response message to join a party.
 */
public class PartyInviteResponseMessage implements ReceivableMessage {
    private boolean accepted;
    private String from;
    private String id;
    private String target;

    public PartyInviteResponseMessage(String from, String id, boolean accepted) {
        this.from = from;
        this.id = id;
        this.accepted = accepted;
    }

    public PartyInviteResponseMessage setTarget(String account) {
        this.target = account;
        return this;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getFrom() {
        return from;
    }

    public String getParty() {
        return id;
    }

    @Override
    public String target() {
        return target;
    }

    @Override
    public String route() {
        return "party_invite_response";
    }
}
