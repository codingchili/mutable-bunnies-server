package com.codingchili.social.model;

import com.codingchili.common.ReceivableMessage;

/**
 * @author Robin Duda
 * <p>
 * Chat message for party members.
 */
public class PartyChatMessage implements ReceivableMessage {
    private boolean party = true;
    private String source;
    private String text;
    private String target;

    public PartyChatMessage(String from, String text) {
        this.source = from;
        this.text = text;
    }

    public PartyChatMessage setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getSource() {
        return source;
    }

    public String getText() {
        return text;
    }

    public boolean isParty() {
        return party;
    }

    @Override
    public String target() {
        return target;
    }

    @Override
    public String route() {
        return "party_chat";
    }
}
