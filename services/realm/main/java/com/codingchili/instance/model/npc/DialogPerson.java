package com.codingchili.instance.model.npc;

import com.codingchili.instance.model.entity.SimpleCreature;

/**
 * @author Robin Duda
 *
 * For testing, starts the test dialog.
 */
public class DialogPerson extends SimpleCreature implements DialogBehaviour {

    @Override
    public String getDialogId() {
        return "tutor";
    }
}
