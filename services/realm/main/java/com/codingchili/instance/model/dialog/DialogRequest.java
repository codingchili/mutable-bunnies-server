package com.codingchili.instance.model.dialog;

import com.codingchili.instance.model.events.Event;
import com.codingchili.instance.model.events.EventType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Robin Duda
 *
 * Dialog message for use in the API.
 */
public class DialogRequest implements Event {
    private Set<Line> lines = new HashSet<>();
    private String text;
    private String targetId;
    private String next;
    private boolean end;

    public static DialogRequest from(ActiveDialog dialog) {
        DialogRequest request = new DialogRequest();
        request.text = dialog.text();
        request.end = dialog.isEnded();
        request.lines = dialog.lines();
        request.targetId = dialog.target().getId();
        return request;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public DialogRequest setLines(Set<Line> options) {
        this.lines = options;
        return this;
    }

    public Set<Line> getLines() {
        return lines;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getNext() {
        return next;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public boolean getEnd() {
        return end;
    }

    public String getTargetId() {
        return targetId;
    }

    public DialogRequest setTargetId(String targetId) {
        this.targetId = targetId;
        return this;
    }

    @Override
    public EventType getRoute() {
        return EventType.dialog;
    }
}
