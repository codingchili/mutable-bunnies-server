package com.codingchili.instance.model.designer;

import com.codingchili.instance.model.entity.Point;
import com.codingchili.instance.model.entity.SpawnConfig;
import com.codingchili.instance.model.events.SpawnType;

public class DesignerRequest {
    private SpawnType type;
    private String id;
    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SpawnType getType() {
        return type;
    }

    public void setType(SpawnType type) {
        this.type = type;
    }

    public SpawnConfig toSpawnConfig() {
        return new SpawnConfig()
                .setId(id)
                .setPoint(new Point(x, y));
    }
}
