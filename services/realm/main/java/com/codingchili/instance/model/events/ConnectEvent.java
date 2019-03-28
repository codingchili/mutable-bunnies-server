package com.codingchili.instance.model.events;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.InstanceSettings;
import com.codingchili.instance.model.entity.*;

import java.util.*;

/**
 * @author Robin Duda
 *
 * On connection this event is emitted to the connecting player.
 * It contains all entities in the current instance.
 */
public class ConnectEvent implements Event {
    private Collection<Entity> entities;
    private Collection<Creature> creatures;
    private Skybox skybox;
    private String player;
    private String texture;
    private int size;

    public ConnectEvent(GameContext game, String player) {
        InstanceSettings instance = game.instance().settings();
        this.entities = game.entities().all();
        this.creatures  = game.creatures().all();
        this.texture = instance.getTexture();
        this.size = instance.getSize();
        this.skybox = instance.getSkybox();
        this.player = player;
    }

    @Override
    public String getSource() {
        return player;
    }

    @Override
    public EventType getRoute() {
        return EventType.join;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public SpawnEvent.SpawnType getSpawn() {
        return SpawnEvent.SpawnType.SPAWN;
    }

    public Collection<Entity> getEntities() {
        return entities;
    }

    public void setEntities(Collection<Entity> entities) {
        this.entities = entities;
    }

    public Collection<Creature> getCreatures() {
        return creatures;
    }

    public void setCreatures(Collection<Creature> creatures) {
        this.creatures = creatures;
    }

    public Skybox getSkybox() {
        return skybox;
    }

    public void setSkybox(Skybox skybox) {
        this.skybox = skybox;
    }
}
