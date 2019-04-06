/**
 * Handles spells by invoking the SpellHandler API's..
 * @type {Window.Spells}
 */
window.Spells = class Spells {

    constructor() {
        this.gcd = (e) => {};
        this.cooldown = (e) => {};
        this.charge = (e) => {};

        server.connection.setHandler('spell', (event) => this._spell(event));
        server.connection.setHandler('stats', (event) => this._stats(event));
        server.connection.setHandler('cleanse', (event) => this._cleanse(event));
        server.connection.setHandler('affliction', (event) => this._affliction(event));
        server.connection.setHandler('spellstate', (event) => this._spellstate(event));
    }

    init(event) {
        this.state = event.spellState;
    }

    emit() {
        let now = new Date().getTime();

        console.log(this.state);

        for (let spell in this.state.charges) {
            this.charge(spell, Math.trunc(this.state.charges[spell]));
        }

        for (let spell in this.state.cooldowns) {
            let cooldown = this.state.cooldowns[spell] - now;

            if (cooldown > 0) {
                this.cooldown(spell, cooldown);
            }
        }
    }

    /**
     * @param event emitted by the server when spell casting state changes.
     * @private
     */
    _spell(event) {
        let now = new Date().getTime();

        if (game.lookup(event.source).isPlayer) {
            if (event.cycle === 'CASTED') {
                if (now < event.gcd) {
                    this.gcd(event.gcd - now);
                }

                if (now < event.cooldown) {
                    this.cooldown(event.spell, event.cooldown - now);
                }
            }
            this.charge(event.spell, event.charges);
        }

    }

    /**
     * @param event handles events emitted by the server when a charge is gained.
     * @private
     */
    _spellstate(event) {
        this.charge(event.spell, event.charges);
    }

    /**
     * @param event an event that is emitted whenever an entity is afflicted.
     * @private
     */
    _affliction(event) {
        let current = game;
        let target = game.lookup(event.targetId);

        let affliction = event;
        affliction.effect = this._activateEffectByAffliction(target, affliction);

        affliction.reference = Math.random().toString(36).substring(7);
        target.afflictions.list.push(affliction);
        target.stats = event.stats;

        setTimeout(() => {
            let afflictions = target.afflictions.list;

            if (current.isPlaying) {
                for (let i = 0; i < afflictions.length; i++) {
                    if (afflictions[i].reference === affliction.reference) {
                        target.afflictions.list.splice(i, 1);

                        if (target.isPlayer) {
                            application.characterUpdate(target);
                        }
                    }
                }
            }
        }, Math.trunc(affliction.duration * 1000));

        if (target.isPlayer) {
            application.characterUpdate(target);
        }
    }

    _disableEffects(affliction) {
        game.particles.stop(affliction.effect);
    }

    _activateEffectByAffliction(target, affliction) {
        switch (affliction.id) {
            case "poison":
                return game.particles.following('cloud', target, affliction.duration);
            case "regeneration":
                return game.particles.following('leaf', target, affliction.duration);
            case "haste":
                return game.particles.following('burst', target, affliction.duration);
        }
    }

    /**
     * @param event an event that causes an affliction to be cleared.
     * @private
     */
    _cleanse(event) {
        let target = game.lookup(event.targetId);
        target.stats = event.stats;
        target.afflictions = target.afflictions.filter((value) => {
            this._disableEffects(value);
            return !event.cleansed.contains(value.name);
        });

        if (target.isPlayer) {
            application.characterUpdate(target);
        }
    }

    /**
     * @param event an event that contains player stats.
     * @private
     */
    _stats(event) {
        let target = game.lookup(event.targetId);
        target.stats = event.stats;

        if (target.isPlayer) {
            application.characterUpdate(target);
        }
    }

    /**
     * Sends a spell casting request to the server.
     * @param callback on response from the server.
     * @param spellId the spell to be cast.
     * @param spellTarget the target of the spell.
     */
    cast(callback, spellId, spellTarget) {
        server.connection.send('cast', {
            spellId: spellId,
            spellTarget: spellTarget
        }, callback);
    }

    /**
     * @param callback when global cooldown is triggered.
     */
    onGCD(callback) {
        this.gcd = callback;
    }

    /**
     * @param callback when a spell is set on cooldown.
     */
    onCooldown(callback) {
        this.cooldown = callback;
    }

    /**
     * @param callback when a new spell charge is gained.
     */
    onCharge(callback) {
        this.charge = callback;
    }
};