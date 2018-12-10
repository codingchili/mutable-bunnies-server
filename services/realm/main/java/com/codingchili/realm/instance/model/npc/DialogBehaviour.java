package com.codingchili.realm.instance.model.npc;

import com.codingchili.realm.instance.model.entity.Entity;

import java.util.Optional;

/**
 * @author Robin Duda
 *
 * Implements the dialog behaviour.
 */
public interface DialogBehaviour {

    /**
     * @return the ID of the dialog.
     */
    String getDialogId();

    static Optional<String> getDialog(Entity entity) {
        if (entity instanceof DialogBehaviour) {
            return Optional.of(((DialogBehaviour) entity).getDialogId());
        } else {
            return Optional.empty();
        }
    }
}
