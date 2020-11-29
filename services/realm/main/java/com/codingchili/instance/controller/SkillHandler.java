package com.codingchili.instance.controller;

import com.codingchili.core.protocol.Api;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.skills.*;
import com.codingchili.instance.transport.InstanceRequest;

/**
 *
 */
public class SkillHandler implements GameHandler {
    private SkillEngine engine;
    private GameContext game;

    public SkillHandler(GameContext game) {
        this.game = game;
        this.engine = game.skills();
    }

    @Api
    public void skill_info(InstanceRequest request) {
        request.write(new SkillDetailsEvent(engine.details()));
    }

    @Api
    public void player_skills(InstanceRequest request) {
        PlayerCreature player = game.getById(request.target());
        request.write(new SkillStateEvent(player));
    }

    @Api
    public void skill_mine(InstanceRequest request) {
        SkillRequest skill = request.raw(SkillRequest.class);
        PlayerCreature player = game.getById(request.target());
        game.spells().cast(player, skill.getSkillTarget(), SkillType.mining.name());
    }

    @Api
    public void skill_farming(InstanceRequest request) {
        SkillRequest skill = request.raw(SkillRequest.class);
        PlayerCreature player = game.getById(request.target());
        game.spells().cast(player, skill.getSkillTarget(), SkillType.farming.name());
    }
}
