package com.codingchili.instance.model.designer;

import com.codingchili.instance.model.entity.SpawnConfig;
import com.codingchili.instance.model.events.SpawnType;

public class DesignerRequest extends SpawnConfig {
    private SpawnType type;

    public SpawnType getType() {
        return type;
    }

    public void setType(SpawnType type) {
        this.type = type;
    }
}
