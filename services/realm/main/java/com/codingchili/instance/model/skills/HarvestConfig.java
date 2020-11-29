package com.codingchili.instance.model.skills;

import com.codingchili.core.storage.Storable;

/**
 * Configuration used for harvesting resources.
 */
public class HarvestConfig implements Storable {
    private String id;
    //  skill required to harvest the resource.
    private SkillType skill;
    // level of the skill required to harvest the resource.
    private int level;
    // amount of experience granted to player on completion.
    private int experience;
    // chance of successfully harvesting, either per resource or per action.
    private float success;
    // base chance of failing to harvest the resource, resulting in
    // a consequence to the player.
    private float fail;
    // the time required to harvest the resource.
    private int time;

    @Override
    public String getId() {
        return id;
    }

    public SkillType getSkill() {
        return skill;
    }

    public void setSkill(SkillType skill) {
        this.skill = skill;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public float getSuccess() {
        return success;
    }

    public void setSuccess(float success) {
        this.success = success;
    }

    public float getFail() {
        return fail;
    }

    public void setFail(float fail) {
        this.fail = fail;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}