package com.codingchili.social.model;

import com.codingchili.common.ReceivableMessage;

/**
 * @author Robin Duda
 * <p>
 * Chat message for party members.
 */
public class PartyChatMessage implements ReceivableMessage {
    private String sender;
    private String message;
    private String target;

    public PartyChatMessage(String from, String message) {
        this.sender = from;
        this.message = message;
    }

    public PartyChatMessage setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
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
