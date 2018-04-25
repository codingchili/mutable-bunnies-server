package com.codingchili.realm.model;

import com.codingchili.common.Strings;
import com.codingchili.realm.instance.model.entity.PlayerCreature;

import java.util.Collection;

/**
 * @author Robin Duda
 *
 * An update sent from the realm to the client when listing characters
 * available on the currently connected realm.
 */
public class CharacterList {
    private Collection<PlayerCreature> characters;

    public CharacterList(Collection<PlayerCreature> characters) {
        this.characters = characters;
    }

    public String getRoute() {
        return Strings.CLIENT_CHARACTER_LIST;
    }

    public Collection<PlayerCreature> getCharacters() {
        return characters;
    }

    public void setCharacters(Collection<PlayerCreature> characters) {
        this.characters = characters;
    }
}
