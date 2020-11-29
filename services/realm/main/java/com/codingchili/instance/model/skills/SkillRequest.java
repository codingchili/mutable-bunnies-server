package com.codingchili.instance.model.skills;

import com.codingchili.instance.model.spells.SpellTarget;

public class SkillRequest {
    private SpellTarget skillTarget;

    public SpellTarget getSkillTarget() {
        return skillTarget;
    }

    public void setSkillTarget(SpellTarget skillTarget) {
        this.skillTarget = skillTarget;
    }
}
