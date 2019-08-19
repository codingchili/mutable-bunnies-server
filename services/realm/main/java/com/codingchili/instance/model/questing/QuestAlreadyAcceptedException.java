package com.codingchili.instance.model.questing;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Fails when the player already has accepted the same quest once.
 * This can happen if dialogs are inconsistent or handlers are changed.
 * <p>
 * It will also occur if a quest is started from an item, and a player
 * happens to have acquired multiple copies of this item.
 */
public class QuestAlreadyAcceptedException extends CoreRuntimeException {

    public QuestAlreadyAcceptedException(Quest quest) {
        super("Quest " + quest.getId() + " has already been accepted.");
    }
}
