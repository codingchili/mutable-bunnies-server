package com.codingchili.instance.controller;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.questing.QuestEngine;
import com.codingchili.instance.model.questing.QuestRequest;
import com.codingchili.instance.transport.InstanceRequest;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 *
 * Handles retrieval of the quest log items.
 */
public class QuestHandler implements GameHandler {
    private GameContext game;
    private QuestEngine quests;

    public QuestHandler(GameContext game) {
        this.quests = game.quests();
        this.game = game;
    }

    @Api
    public void quest_list(InstanceRequest request) {
        request.write(quests.list(game.getById(request.target())));
    }

    @Api
    public void quest_details(InstanceRequest request) {
        QuestRequest quest = request.raw(QuestRequest.class);
        request.write(quests.log(game.getById(request.target()), quest.getQuestId()));
    }
}
