package com.codingchili.instance.model.skills;

import com.codingchili.core.context.CoreRuntimeException;

public class HarvestConfigNotFound extends CoreRuntimeException {
    public HarvestConfigNotFound(String skillId) {
        super(String.format("Harvest config with id `%s` not found.", skillId));
    }
}
