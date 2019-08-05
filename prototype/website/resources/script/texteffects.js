window.TextEffects = class TextEffects {

    constructor() {
        this.counters = [];
        this.last = performance.now();
        this.effects = {
            'physical': this.physical,
            'heal': this.heal,
            'magic': this.magic,
            'experience': this.experience,
            'true': this.trueDamage,
            'chat': this.chat,
            'poison': this.poison
        };

        server.connection.setHandler('error', (event) => {
            this.effects['chat'](game.player, {
                text: event.message
            });
        });

        server.connection.setHandler('damage', event => {
            let target = game.lookup(event.targetId);

            target.stats.health += event.value;

            if (target.isPlayer) {
                application.characterUpdate(target);
            }

            event.value = event.value.toFixed(1);
            this.effects[event.damage](target, event);
        });
    }

    update(delta) {
        for (let i = 0; i < this.counters.length; i++) {
            let counter = this.counters[i];

            if (!counter.visible) {
                counter.ttl = 0;
            }

            counter.ttl -= delta;

            if (counter.ttl <= 0 && counter.alpha <= 0) {
                game.stage.removeChild(counter);
                this.counters.splice(i, 1);
            } else {
                counter.speed *= counter.slowdown;
                counter.x += counter.speed * Math.cos(counter.dir) * delta;
                counter.y += counter.speed * Math.sin(counter.dir) * delta;

                if (counter.ttl > 0) {
                    counter.alpha += counter.fade_in * delta;
                }

                if (counter.ttl < 0) {
                    counter.alpha -= counter.fade_out * delta;
                }

                if (counter.alpha > 1.0) {
                    counter.alpha = 1.0;
                }
                if (counter.alpha < 0.0) {
                    counter.alpha = 0.0;
                }
            }
        }
    }

    style(options) {
        options = options || this.options();

        return new PIXI.TextStyle({
            fontFamily: 'Verdana',
            fontSize: options.size || 12,
            //fontWeight: 'bold',
            fill: [options.begin, options.end],
            stroke: '#000000',
            strokeThickness: 4,
            wordWrap: true,
            wordWrapWidth: 440
        });
    }

    options() {
        return {
            begin: '#ffffff',
            end: '#ffffff',
        }
    }

    _create(target, text, options) {
        let style = this.style(options);
        let counter = new PIXI.Text(text, style);
        counter.dir = (6.14 / 360) * Math.random() * 360;

        if (options.critical) {
            style.fontSize = 16;
            counter.ttl = options.ttl || 2.2;
        } else {
            counter.ttl = options.ttl || 1.2;
        }

        counter.x = target.x - (counter.width / 2) + (target.width / 3) * Math.cos(counter.dir);
        counter.y = target.y - (target.height / 2) + (target.height / 4) * Math.sin(counter.dir);
        counter.alpha = 0.18;
        counter.speed = 192;
        counter.slowdown = 0.825;
        counter.fade_in = 25;
        counter.fade_out = 8;

        // newer texts always on top!
        counter.layer = performance.now();

        if (options.float) {
            counter.dir = (6.14 / 360) * 270;
            counter.x = target.x - (counter.width / 2);
            counter.y = (target.y * 1.01) - target.height;
        }

        this.counters.push(counter);
        game.stage.addChild(counter);
        return counter;
    }

    physical(target, event) {
        game.texts._create(target, event.value, {
            begin: '#ff1800',
            end: '#ff0f00',
        });
    }

    heal(target, event) {
        game.texts._create(target, '+' + event.value, {
            begin: '#06ff00',
            end: '#13ff01',
            float: true
        });
    }

    magic(target, event) {
        game.texts._create(target, event.value, {
            begin: '#ff03f5',
            end: '#ff00cf',
        });
    }

    experience(target, event) {
        game.texts._create(target, '+' + event.value, {
            begin: '#ffc200',
            end: '#ffc200',
            float: true,
            ttl: 2.6,
            critical: true
        });
    }

    trueDamage(target, event) {
        game.texts._create(target, event.value, {
            begin: '#ffeaf9',
            end: '#ff0702',
        });
    }

    poison(target, event) {
        game.texts._create(target, event.value, {
            begin: '#ffcc00',
            end: '#0bb001',
        });
    }

    chat(target, event) {
        game.texts._create(target, event.text, {
            begin: event.color1 || '#eaeaea',
            end: event.color2 || '#ffffff',
            float: true,
            // longer chat messages has longer lifetime.
            ttl: 1.325 + event.text.length * 0.065
        });
    }

    levelUp(target) {
        game.texts._create(target, `Level Up!`, {
            size: 18,
            float: true,
            ttl: 4.0,
            begin: '#ffc200',
            end: '#ffc200'
        });
    }
};