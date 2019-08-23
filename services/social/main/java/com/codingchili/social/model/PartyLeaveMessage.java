package com.codingchili.social.model;

import com.codingchili.common.ReceivableMessage;

/**
 * @author Robin Duda
 * <p>
 * A list of updated party members.
 */
public class PartyLeaveMessage implements ReceivableMessage {
    private String member;
    private String target;
    private String id;

    public PartyLeaveMessage(String member, String partyId) {
        this.member = member;
        this.id = partyId;
    }

    public PartyLeaveMessage setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getId() {
        return id;
    }

    public String getMember() {
        return member;
    }

    @Override
    public String target() {
        return target;
    }

    @Override
    public String route() {
        return "party_leave";
    }
}
