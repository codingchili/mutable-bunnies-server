package com.codingchili.instance.model.admin;

import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

import com.codingchili.core.context.CommandParser;

/**
 * @author Robin Duda
 *
 * An admin command from a client.
 */
public class AdminEvent implements Event {
    private String target;
    private String entity;
    private String command;
    private String id;
    private String line;
    private String message;
    private Vector vector;
    private int quantity;

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;

        if (line != null) {
            CommandParser parser = new CommandParser(line.split(" "));
            parser.getCommand().ifPresent(command -> {
                this.command = command;
                this.entity = parser.getValue("-e").orElse(this.entity);
                this.message = String.join(" ", parser.getAllValues("--message"));
                this.id = parser.getValue("-i").orElse(this.id);
                parser.getValue("-q").ifPresent(value -> {
                    this.quantity = Integer.parseInt(value);
                });
            });
        }
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return this.target;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public EventType getRoute() {
        return EventType.admin;
    }
}
