package com.codingchili.instance.model.questing;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;

import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Implementation of the questing engine, loading, progressing, listing etc.
 */
public class QuestEngine {
    private QuestDB quests;
    private GameContext game;

    public QuestEngine(GameContext game) {
        this.game = game;
        this.quests = new QuestDB(game.instance());
    }

    /**
     * @param creature
     * @param questId
     */
    public void start(Creature creature, String questId) {
        quests.getById(questId).ifPresent(quest -> {
            isPlayer(creature).ifPresent(player -> {
                QuestState state = player.getQuests();
                if (state.has(quest.getId())) {
                    throw new QuestAlreadyAcceptedException(quest);
                } else {
                    state.add(quest);
                    player.handle(new QuestAddEvent(player, quest));
                    execute(quest.getFirstStage().getEnter(), player);
                }
            });
        });
    }

    private void execute(Scripted event, PlayerCreature creature) {
        if (event != null) {
            event.apply(new Bindings()
                    .setSource(creature)
                    .setContext(game)
            );
        }
    }


    /**
     * @param creature
     * @return
     */
    public Collection<QuestEntry> list(Creature creature) {
        return isPlayer(creature).map(player -> {
            Collection<QuestEntry> list = new ArrayList<>();

            player.getQuests().asMap().forEach((questId, progress) -> {
                quests.getById(questId).ifPresent(quest -> {
                    list.add(new QuestEntry()
                            .setId(quest.getId())
                            .setName(quest.getName())
                            .setCompleted(progress.isComplete())
                    );
                });
            });
            return list;
        }).orElseThrow(
                () -> new QuestRequiresPlayerCreatureException(creature.getId()));
    }

    /**
     * @param source
     * @param questId
     */
    public void advance(Creature source, String questId) {
        isPlayer(source).ifPresent(player -> {
            QuestProgress progress = player.getQuests().getById(questId);

            quests.getById(questId).ifPresent(quest -> {
                // initialize next stage.
                quest.getNextStage(progress.getStage()).ifPresent(stage -> {
                    // complete previous stage - only if we are moving to the next
                    // to prevent a single step from being completed multiple times.
                    quest.getStageById(progress.getStage()).ifPresent(previous -> {
                        execute(previous.getCompleted(), player);
                    });

                    execute(stage.getEnter(), player);
                    progress.setStage(stage.getId());

                    // mark as completed if we're on the final stage.
                    if (quest.isFinalStage(progress.getStage())) {
                        // completion stage is always executed instantly.
                        execute(stage.getCompleted(), player);
                        progress.setComplete(true);
                        source.handle(new QuestCompleteEvent(quest));
                    } else {
                        // notify player quest log updated.
                    }
                });
            });
        });
    }

    /**
     * @param creature
     * @param questId
     * @return
     */
    public QuestLog log(Creature creature, String questId) {
        return isPlayer(creature).map(player -> {
            Optional<Quest> quest = quests.getById(questId);

            return quest.map(value ->
                    new QuestLog(value, player.getQuests().getById(questId)))
                    .orElse(null);

        }).orElseThrow(() -> new InvalidQuestException("No such quest " + questId));
    }

    private Optional<PlayerCreature> isPlayer(Creature creature) {
        if (creature instanceof PlayerCreature) {
            return Optional.of((PlayerCreature) creature);
        } else {
            return Optional.empty();
        }
    }
}
