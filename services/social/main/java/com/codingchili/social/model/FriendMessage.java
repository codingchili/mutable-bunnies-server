package com.codingchili.social.model;

import com.codingchili.common.ReceivableMessage;
import com.codingchili.social.controller.SocialRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Robin Duda
 *
 * A message between friends.
 */
public class FriendMessage implements ReceivableMessage {
    private static final String FRIEND_MESSAGE = "friend_message";
    private String from;
    private String to;
    private String message;

    public FriendMessage() {}

    public FriendMessage(SocialRequest request) {
        this.from = request.account();
        this.to = request.friend();
        this.message = request.message();
    }

    public String getFrom() {
        return from;
    }

    public FriendMessage setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public FriendMessage setTo(String to) {
        this.to = to;return this;

    }

    public String getMessage() {
        return message;
    }

    public FriendMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    @JsonProperty("target")
    public String target() {
        return to;
    }

    @Override
    @JsonProperty("route")
    public String route() {
        return FRIEND_MESSAGE;
    }
}
