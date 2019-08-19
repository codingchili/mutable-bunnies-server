package com.codingchili.instance.model.questing;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * Thrown when quest operations are attempted on non player-creatures.
 */
public class QuestRequiresPlayerCreatureException extends CoreRuntimeException {

    public QuestRequiresPlayerCreatureException(String player) {
        super("Quests require a player creature, " + player + " is not.");
    }
}
