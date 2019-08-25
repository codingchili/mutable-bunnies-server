/**
 *
 * @type {Window.SpellEffects}
 */
window.SpellEffects = class SpellEffects {

    constructor() {
        this.effects = {};
    }

    /**
     * Activates affliction effects for the given target/affliction.
     * @param target the target of the affliction.
     * @param active the affliction being applied.
     * @returns {string} a unique id of the effect so that it can be cancelled.
     * @private
     */
    affliction(target, active) {
        active.reference = Math.random().toString(36).substring(7);

        switch (active.affliction.id) {
            case "poison":
                return game.particles.following('cloud', target, active.duration);
            case "regeneration":
                return game.particles.following('leaf', target, active.duration);
            case "haste":
                return game.particles.following('burst', target, active.duration);
            case "ethereal": {
                target.tint = 0x000000;

                this.effects[active.reference] = {
                    update: (delta) => {
                        if (target.alpha > 0) {
                            target.alpha = 0.4;
                        }
                        return true;
                    },
                    complete: () => {
                        target.alpha = 1.0;
                        target.tint = 0xffffff;
                    }
                };
            }
        }
    }

    /**
     *
     * @param affliction
     */
    stop(affliction) {
        let effect = this.effects[affliction.reference];

        if (effect && effect.complete) {
            effect.complete();
            delete this.effects[affliction.reference];
        }

        game.particles.stop(affliction.effect);
    }

    /**
     * Activates spell effects for a spell, during channeling or activation.
     * @param event the event that contains information about the spell being activated.
     * @returns {string} a unique id of the effect so that it can be cancelled.
     * @private
     */
    casting(event) {
        let entity = game.lookup(event.source);
        entity.state.setAnimation(0, 'push-attack', true);
        entity.state.oldTimeScale = entity.state.timeScale;
        entity.state.timeScale = 0.6;
    }

    /**
     *
     * @param event
     */
    damage(event) {
        console.log(event);
        if (event.effect === 'dagger') {
            let target = event.target;
            let source = event.source;

            Loader.load((sprite) => {
                Object.assign(sprite, {});

                console.log(event.target);

                sprite.x = source.x;
                sprite.y = source.y;
                sprite.velocity = 672;
                sprite.direction = 180;
                sprite.scale.x = 0.8;
                sprite.scale.y = 0.8;
                sprite.pivot.y = sprite.height / 2;
                sprite.pivot.x = sprite.width / 2;
                sprite.layer = target.model.layer;
                sprite.id = Math.random().toString(36).substring(7);

                sprite.update = (delta) => {
                    sprite.direction = (Math.atan2(sprite.y - (target.y - target.height / 2), target.x - sprite.x)) + 3.14 / 2;
                    sprite.x += Math.sin(sprite.direction) * sprite.velocity * delta;
                    sprite.y += Math.cos(sprite.direction) * sprite.velocity * delta;
                    sprite.rotation += 18 * delta;
                    return (Math.hypot(Math.abs(sprite.x - target.x), Math.abs(sprite.y - (target.y - (target.height / 2)))) > 16);
                };

                this.effects[sprite.id] = sprite;
                game.stage.addChild(sprite);

            }, 'game/spells/dagger.png').begin();
        }
    }

    update(delta) {
        for (let id in this.effects) {
            let effect = this.effects[id];

            if (!effect.update(delta)) {
                delete this.effects[effect];
                game.stage.removeChild(effect);
            }
        }
    }

    /**
     *
     * @param event
     * @returns {string|void}
     */
    casted(event) {
        if (event.spell === 'dagger') {
            sound.play('dagger.mp3')
        }

        if (event.spell === 'potent_venom') {
            let target = event.spellTarget;
            sound.play('glass_break.mp3');

            return game.particles.spawn('cloud', {
                x: target.vector.x,
                y: target.vector.y
            }, 12.0); // get TTL from spell config?
        }

        if (event.spell === 'regeneration') {
            sound.play('leaves.mp3');
        }

        if (event.spell === 'shadow_step') {
            let target = game.lookup(event.source);
            sound.play('woosh.mp3');

            // sets starting point to old position.
            let start = {
                x: target.x,
                y: target.y
            };

            // set target point to new position with updates.
            target.x = event.spellTarget.vector.x;
            target.y = event.spellTarget.vector.y;
            target.alpha = 0.0;

            game.particles.moving('flash', start, {
                destination: target,
                velocity: 1200.0,
                complete: () => {
                    target.alpha = 1.0;
                }
            });
        }
    }
};

