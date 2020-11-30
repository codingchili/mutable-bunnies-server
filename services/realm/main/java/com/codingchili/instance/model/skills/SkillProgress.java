package com.codingchili.instance.model.skills;

public class SkillProgress {
    private SkillType type;
    private int level = 1;
    private int experience = 0;
    private int nextlevel = 100; // apply scaling on load?

    public SkillProgress(SkillType type) {
        this.type = type;
    }

    public SkillType getType() {
        return type;
    }

    public void setType(SkillType type) {
        this.type = type;
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

    public int getNextlevel() {
        return nextlevel;
    }

    public void setNextlevel(int nextlevel) {
        this.nextlevel = nextlevel;
    }

    public void levelUp(int next) {
        level++;
        experience = 0;
        nextlevel = next;
    }
}
