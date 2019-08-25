/**
 * Handles spells by invoking the SpellHandler API's..
 * @type {Window.Spells}
 */
window.CYCLE_CASTED = 'CASTED';
window.CYCLE_CASTING = 'CASTING';
window.CYCLE_INTERRUPTED = 'INTERRUPTED';
window.CYCLE_CANCELLED = 'CANCELLED';

window.Spells = class Spells {

    constructor() {
        this.effects = new SpellEffects();
        this.gcd = (e) => {
        };
        this.cooldown = (e) => {
        };
        this.charge = (e) => {
        };

        input.onKeysListener({
            down: () => {
                if (this.loaded) {
                    let marker = this.loaded.marker;
                    let loaded = this.loaded;

                    this._startCast(loaded.callback, loaded.spellId, {
                        vector: {
                            x: marker.x,
                            y: marker.y
                        }
                    });
                    this._cancel();
                }
            }
        }, [input.LMB]);

        input.onKeysListener({
            down: () => {
                this._cancel();
            }
        }, [input.ESCAPE]);

        server.connection.setHandler('spell', (event) => this._spell(event));
        server.connection.setHandler('stats', (event) => this._stats(event));
        server.connection.setHandler('cleanse', (event) => this._cleanse(event));
        server.connection.setHandler('affliction', (event) => this.affliction(event));
        server.connection.setHandler('spellstate', (event) => this._spellstate(event));
        server.connection.setHandler('damage', event => this._damage(event));
    }


    /**
     * @param event initializes the spell handler with the given player spell state.
     */
    init(event) {
        this.state = event.spellState;
        this.spells = application.realm.spells;
        this.classes = application.realm.classes;
    }

    _damage(event) {
        let target = game.lookup(event.targetId);
        let source = game.lookup(event.sourceId);

        target.stats.health += event.value;

        if (target.isPlayer) {
            application.characterUpdate(target);
        }
        game.publish('character-update', target);

        if (event.value < 20) {
            event.value = event.value.toFixed(1);
        } else {
            event.value = event.value.toFixed(0);
        }
        game.texts.effects[event.type](target, event);

        event.target = target;
        event.source = source;
        this.effects.damage(event);
    }

    _cancel() {
        if (this.loaded) {
            game.stage.removeChild(this.loaded.marker);
            delete this.loaded;
        }
    }

    /**
     * Emits the initial state of charges and cooldowns.
     */
    emit() {
        let now = new Date().getTime();

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
     * @param spellId the id of the spell to find.
     * @returns {*} a spell configuration object.
     */
    getById(spellId) {
        for (let i = 0; i < this.spells.length; i++) {
            if (this.spells[i].id === spellId) {
                return this.spells[i];
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
            if (event.cycle === CYCLE_CASTED) {
                if (now < event.gcd) {
                    this.gcd(event.gcd - now);
                }
                if (now < event.cooldown) {
                    this.cooldown(event.spell, event.cooldown - now);
                }
            }

            if (event.cycle === CYCLE_CASTED) {
                this.charge(event.spell, event.charges);
            }
        }

        if (event.cycle === CYCLE_CASTED) {
            this.effects.casted(event);
        }

        if (event.cycle === CYCLE_INTERRUPTED || event.cycle === CYCLE_CANCELLED || event.cycle === CYCLE_CASTED) {
            let entity = game.lookup(event.source);

            if (entity.velocity === 0) {
                entity.state.setAnimation(0, 'idle', true);
                entity.state.timeScale = entity.state.oldTimeScale;
            }
        }

        if (event.cycle === CYCLE_CASTING) {
            this.effects.casting(event);
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
    affliction(event) {
        let current = game;
        let target = game.lookup(event.targetId);
        let active = event;

        this.effects.affliction(target, active);

        if (!active.loaded) {
            target.stats = active.stats;
            target.afflictions.push(active);
        }

        setTimeout(() => {
            let afflictions = target.afflictions;

            if (current.isPlaying) {
                for (let i = 0; i < afflictions.length; i++) {
                    if (afflictions[i].reference === active.reference) {
                        target.afflictions.splice(i, 1);

                        if (target.isPlayer) {
                            application.characterUpdate(target);
                        }
                        this.effects.stop(active);
                        game.publish('character-update', target);
                    }
                }
            }
        }, Math.trunc(active.duration * 1000));

        if (target.isPlayer) {
            application.characterUpdate(target);
        }
        game.publish('character-update', target);
    }

    /**
     * @param event an event that causes an affliction to be cleared.
     * @private
     */
    _cleanse(event) {
        let target = game.lookup(event.targetId);
        target.stats = event.stats;
        target.afflictions = target.afflictions.filter((value) => {
            this.effects.stop(value);
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

        if (this._statUpdated(target, event, 'level')) {

            if (target.isPlayer) {
                application.publish('notification', {
                    text: `Congratulations! You've reached level ${event.stats['level']}.`,
                    duration: 4500
                });
            }

            game.texts.levelUp(target);
            game.chat.add({text: `${target.name} reached level ${event.stats['level']}!`, system: true});
        } else {
            // only show experience gains if not also a level up event.
            if (this._statUpdated(target, event, 'experience')) {
                let difference = event.stats['experience'] - target.stats['experience'];
                game.texts.experience(target, {value: difference});

                if (target.isPlayer) {
                    game.chat.add({text: `gained ${difference} experience points.`, system: true});
                }
            }
        }

        target.stats = event.stats;

        if (target.isPlayer) {
            application.characterUpdate(target);
        }
        game.publish('character-update', target);
    }

    _statUpdated(target, event, name) {
        let updated = event.stats[name];
        let current = target.stats[name];

        if (updated && current) {
            if (updated > current) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Sends a spell casting request to the server.
     * @param callback on response from the server.
     * @param spellId the spell to be cast.
     * @param spellTarget the target of the spell.
     */
    cast(callback, spellId, spellTarget) {
        let spell = this.getById(spellId);

        if (spell.target === 'area') {
            this._cancel();

            let marker = new PIXI.Graphics();
            marker.layer = 0;
            //marker.lineStyle(2, this._theme(), 0.5);
            marker.lineStyle(2, 0x00b0ff, 0.5);
            marker.drawEllipse(0, 0, 256, 128);
            this.loaded = {
                marker: marker,
                spellId: spellId,
                spellTarget: spellTarget,
                callback: callback
            };
            game.stage.addChild(marker);
        } else {
            this._startCast(callback, spellId, spellTarget);
        }
    }

    _startCast(callback, spellId, spellTarget) {
        server.connection.send('cast', {
            spellId: spellId,
            spellTarget: spellTarget
        }, callback);
    }

    _mouse() {
        return game.renderer.plugins.interaction.mouse.global;
    }

    update(delta) {
        if (this.loaded) {
            let marker = this.loaded.marker;
            marker.x = this._mouse().x + game.camera.x;
            marker.y = this._mouse().y + game.camera.y;
            // todo if marker out of range do something.
        }
        this.effects.update(delta);
    }

    _theme() {
        let theme = this.classes.get(game.player.classId).theme;
        return parseInt(theme.replace('#', '0x'));
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