package com.codingchili.instance.controller;

import com.codingchili.core.protocol.Api;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.skills.*;
import com.codingchili.instance.transport.InstanceRequest;

/**
 * Handler for player skills.
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
        SkillType type = request.raw(SkillMessage.class).getId();
        engine.details(type).ifPresentOrElse(
                (skill) -> request.write(new SkillDetailsEvent(skill)),
                () -> request.error(new SkillConfigNotFound(type))
        );
    }

    @Api
    public void skill_state(InstanceRequest request) {
        PlayerCreature player = game.getById(request.target());
        request.write(new SkillStateEvent(player));
    }
}
