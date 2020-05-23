package com.codingchili.instance.model.admin;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

import com.codingchili.core.context.CommandParser;

/**
 * @author Robin Duda
 *
 * An admin command from a client.
 */
public class AdminEvent implements Event {
    private String entity;
    private String command;
    private String id;
    private String line;
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
                this.entity = parser.getValue("--entity").orElse(null);
                this.id = parser.getValue("--id").orElse(null);
                parser.getValue("--quantity").ifPresent(value -> {
                    this.quantity = Integer.parseInt(value);
                });
            });
        }
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

    @Override
    public EventType getRoute() {
        return EventType.admin;
    }
}
