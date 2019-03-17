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