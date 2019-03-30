window.Spells = class Spells  {

    constructor() {
        this.gcd = () => {};
        this.cooldown = () => {};
        this.charge = () => {};

        server.connection.setHandler('spell', (cast) => {
            let now = new Date().getTime();

            if (game.lookup(cast.source).isPlayer) {
                if (cast.cycle === 'CASTED') {
                    if (now < cast.gcd) {
                        this.gcd(cast.gcd - now);
                    }

                    if (now < cast.cooldown) {
                        this.cooldown(cast.spell, cast.cooldown - now);
                    }
                }
                this.charge(cast.spell, cast.charges);
            }
        });

        server.connection.setHandler('stats', event => {
            let target = game.lookup(event.targetId);
            target.stats = event.stats;

            if (target.isPlayer) {
                application.characterUpdate(target);
            }
        });

        server.connection.setHandler('affliction', event => {
            let current = game;
            let target = game.lookup(event.targetId);

            let affliction = (({name, description, duration}) => ({name, description, duration}))(event);
            affliction.id = Math.random().toString(36).substring(7);

            target.afflictions.list.push(affliction);

            setTimeout(() => {
                let afflictions = target.afflictions.list;

                if (current.isPlaying) {
                    for (let i = 0; i < afflictions.length; i++) {
                        if (afflictions[i].id === affliction.id) {
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
        });

        server.connection.setHandler('spellstate', (state) => {
            console.log(state);
            this.charge(state.spell, state.charges);
        });
    }

    cast(callback, spellName, spellTarget) {
        server.connection.send('cast', {
            spellName: spellName,
            spellTarget: spellTarget
        }, callback);
    }

    onGCD(callback) {
        this.gcd = callback;
    }

    onCooldown(callback) {
        this.cooldown = callback;
    }

    onCharge(callback) {
        this.charge = callback;
    }
};