package com.codingchili.realm.instance.transport;

import com.codingchili.realm.instance.model.events.Event;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.Connection;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_ROUTE;

/**
 * @author Robin Duda
 *
 * Player requests are sent from an instance to the RealmServer which forwards
 * the message to the appropriate client as defined by the #{@link #receiver()}.
 */
public class PlayerRequest implements Request {
    private JsonObject json;
    private Future<Object> future;
    private Event event;
    private String receiver;

    /**
     * Creates a new player request without a success/fail callback.
     * @param event this is the event to be propagated to a client.
     * @param receiver this is the name of the account to receive the event.
     */
    public PlayerRequest(Event event, String receiver) {
        this(Future.future(), event, receiver);
    }

    /**
     * Creates a  new player request with a success/fail callback.
     * @param future this will be called when the realmserver sends the message, if the
     *               message sending succeeds this will be completed successfully.
     * @param event the event to be propagated to a client.
     * @param receiver this is the name of the account to receive the event.
     */
    public PlayerRequest(Future<Object> future, Event event, String receiver) {
        this.future = future;
        this.receiver = receiver;
        this.event = event;
    }

    @Override
    public Connection connection() {
        throw new UnsupportedOperationException("No connections can be created from a LocalRequest.");
    }

    /**
     * @return name of the account to receive the event.
     */
    public String receiver() {
        return receiver;
    }

    /**
     * @return the event that is to be propagated to the given receiver.
     */
    public Event getEvent() {
        return event;
    }

    @Override
    public String route() {
        return event.getType().toString();
    }

    @Override
    public JsonObject data() {
        if (json == null) {
            json = Serializer.json(event);
            json.put(PROTOCOL_ROUTE, route());
        }
        return json;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void write(Object object) {
        future.complete(object);
    }
}
