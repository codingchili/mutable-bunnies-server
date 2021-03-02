package com.codingchili.instance.model.skills;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 */
public class SkillNotLearnedException extends CoreRuntimeException {
    public SkillNotLearnedException(SkillType skill) {
        super("Skill not learned " + skill.name());
    }
}
