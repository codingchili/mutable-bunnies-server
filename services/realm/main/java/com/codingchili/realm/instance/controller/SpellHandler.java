package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.*;
import com.codingchili.realm.instance.model.requests.SpellCastRequest;
import com.codingchili.realm.instance.model.requests.SpellCastResponse;
import com.codingchili.realm.instance.model.spells.SpellEngine;
import com.codingchili.realm.instance.model.spells.SpellResult;
import com.codingchili.realm.instance.transport.InstanceRequest;

import java.util.Optional;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.Api;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 * <p>
 * A spell handler to handle spell info and spell casting requests.
 */
public class SpellHandler implements GameHandler {
    private GameContext game;
    private SpellEngine spells;

    /**
     * @param game the game context to run the handler on.
     */
    public SpellHandler(GameContext game) {
        this.game = game;
        this.spells = game.spells();
    }

    @Api
    public void list(InstanceRequest request) {
        PlayerCreature creature = game.getById(request.target());
        Optional<PlayableClass> aClass = game.classes().getByName(creature.getClassName());

        if (aClass.isPresent()) {
            request.write(aClass.get().getSpells().stream()
                    .map(game.spells()::getSpellByName)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList()));
        } else {
            request.error(new CoreRuntimeException("Player class unavailable: " + creature.getClassName()));
        }
    }

    @Api
    public void cast(InstanceRequest request) {
        SpellCastRequest cast = request.raw(SpellCastRequest.class);
        Creature caster = game.getById(request.target());
        SpellResult result = spells.cast(caster, cast.getSpellTarget(), cast.getSpellName());
        request.write(new SpellCastResponse(result));
    }

    @Api
    public void spell(InstanceRequest request) {
        request.write(spells.getSpellByName(request.data().getString("spellName")));
    }
}
