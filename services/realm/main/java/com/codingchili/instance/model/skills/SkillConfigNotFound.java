package com.codingchili.instance.model.skills;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * Thrown when a skill is not found.
 */
public class SkillConfigNotFound extends CoreRuntimeException {
    public SkillConfigNotFound(SkillType type) {
        super(String.format("Skill config with id `%s` not found.", type.name()));
    }
}
