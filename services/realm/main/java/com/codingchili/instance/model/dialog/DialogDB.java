package com.codingchili.instance.model.dialog;

import com.codingchili.instance.model.npc.DB;

import java.util.Optional;

import com.codingchili.core.context.CoreContext;

/**
 * A database of all dialogs in the system.
 */
public class DialogDB {
    private static final String CONF_PATH = "conf/game/dialog";
    private DB<Dialog> dialogs;

    public DialogDB(CoreContext core) {
        this.dialogs = DB.create(core, Dialog.class, CONF_PATH);
    }

    public Optional<Dialog> getById(String id) {
        return dialogs.getById(id);
    }
}
