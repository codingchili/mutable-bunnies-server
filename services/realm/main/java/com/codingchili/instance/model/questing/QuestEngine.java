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
     * Starts the quest with the given id if it exists and the player does not already
     * have the quest.
     *
     * @param creature the creature that is starting the quest.
     * @param questId  the id of the quest to be started.
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
     * Lists all quests for the given creature.
     *
     * @param creature the creature to list quest state for.
     * @return a list of quest entries.
     */
    public Collection<QuestEntry> list(Creature creature) {
        return isPlayer(creature).map(player -> {
            Collection<QuestEntry> list = new ArrayList<>();

            player.getQuests().getProgress().forEach((questId, progress) -> {
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
     * Attempts to forward the quest cursor for the given quest and character.
     *
     * @param source  the creature that is advancing in the quest sequence.
     * @param questId the id of the quest to advance.
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
     * Retrieves the quest log for the given quest id.
     *
     * @param creature the creature to retrieve the quest log for.
     * @param questId  the quest id for which to retrieve the log.
     * @return the quest log for the given quest id, which includes
     * the description of each completed stage and the first incomplete (current).
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
