package com.codingchili.instance.model.spells;

import com.codingchili.instance.scripting.Scripted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * A spell item from the spell DB.
 */
public class Spell implements Storable, Configurable {
    private SpellAnimation animation = new SpellAnimation();
    private String id = "no_id";
    private String name = "no name";
    private String description = "no description";
    private Boolean mobile = true; // can move and cast?
    private Boolean skill = false; // indicates if skill or spell.
    private Target target = Target.caster; // spell target: caster, area etc.
    private Integer charges = 0;  // number of times the spell can be cast in a sequence without recharge.
    private Integer range = 100; // how far away the target may be.
    private Float interval = 0.5f; // how often to call onProgress and onActive.
    private Float cooldown = 1.0f; // minimum time between casting the spell.
    private Float recharge = 1.0f; // time taken to generate one charge.
    private Float casttime = 0.0f; // the time taken to cast the spell.
    private Float active = 0.0f; // how long the spell is active after casting is completed.
    private Scripted onCastBegin;    // check pre-requisites - must check result.
    private Scripted onCastProgress; // implement for channeled abilities.
    private Scripted onCastComplete; // implement casted spell logic here.
    private Scripted onSpellActive;    // for spells that are active longer than the casting period.

    @Override
    public String getPath() {
        return "conf/game/classes/" + id + CoreStrings.EXT_YAML;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSkill() {
        return skill;
    }

    public void setSkill(Boolean skill) {
        this.skill = skill;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getMobile() {
        return mobile;
    }

    public void setMobile(Boolean mobile) {
        this.mobile = mobile;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Integer getCharges() {
        return charges;
    }

    public void setCharges(Integer charges) {
        this.charges = charges;
    }

    public Float getCooldown() {
        return cooldown;
    }

    public void setCooldown(Float cooldown) {
        this.cooldown = cooldown;
    }

    public Float getCasttime() {
        return casttime;
    }

    public void setCasttime(Float casttime) {
        this.casttime = casttime;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }

    public Float getActive() {
        return active;
    }

    public void setActive(Float active) {
        this.active = active;
    }

    public Float getInterval() {
        return interval;
    }

    public void setInterval(Float interval) {
        this.interval = interval;
    }

    public Float getRecharge() {
        return recharge;
    }

    public void setRecharge(Float recharge) {
        this.recharge = recharge;
    }

    public SpellAnimation getAnimation() {
        return animation;
    }

    public void setAnimation(SpellAnimation animation) {
        this.animation = animation;
    }

    @JsonIgnore
    public Scripted getOnCastBegin() {
        return onCastBegin;
    }

    @JsonProperty("onCastBegin")
    public void setOnCastBegin(Scripted onCastBegin) {
        this.onCastBegin = onCastBegin;
    }

    @JsonIgnore
    public Scripted getOnCastProgress() {
        return onCastProgress;
    }

    @JsonProperty("onCastProgress")
    public void setOnCastProgress(Scripted onCastProgress) {
        this.onCastProgress = onCastProgress;
    }

    @JsonIgnore
    public Scripted getOnCastComplete() {
        return onCastComplete;
    }

    @JsonProperty("onCastComplete")
    public void setOnCastComplete(Scripted onCastComplete) {
        this.onCastComplete = onCastComplete;
    }

    @JsonIgnore
    public Scripted getOnSpellActive() {
        return onSpellActive;
    }

    @JsonProperty("onSpellActive")
    public void setOnSpellActive(Scripted onSpellActive) {
        this.onSpellActive = onSpellActive;
    }
}
