package com.codingchili.instance.model.skills;

public class LearnedSkill {
    private SkillType type;
    private int level;
    private int experience;
    private int nextlevel; // apply scaling on load?

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
