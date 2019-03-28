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

        server.connection.setHandler('stats', event => {
            let target = game.lookup(event.targetId);
            target.stats = event.stats;

            if (target.isPlayer) {
                application.characterUpdate(target);
            }
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

        game.ticker(() => {
            this._update();
        });
    }

    _update() {
        let delta = (performance.now() - this.last) / Game.MS_PER_FRAME;

        for (let i = 0; i < this.counters.length; i++) {
            let counter = this.counters[i];

            if (!counter.visible) {
                // skip updating text that is outside of the screen.
                continue;
            }

            counter.ttl -= 1;

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
        this.last = performance.now();
    }

    _create(target, text, options) {
        let style = new PIXI.TextStyle({
            fontFamily: 'Verdana',
            fontSize: 12,
            //fontWeight: 'bold',
            fill: [options.begin, options.end],
            stroke: '#000000',
            strokeThickness: 4,
            wordWrap: true,
            wordWrapWidth: 440
        });

        let counter = new PIXI.Text(text, style);
        counter.dir = (6.14 / 360) * Math.random() * 360;

        if (options.critical) {
            style.fontSize = 16;
            counter.ttl = options.ttl || 120;
        } else {
            counter.ttl = options.ttl || 100;
        }

        counter.x = target.x - counter.width + (target.width / 3) * Math.cos(counter.dir);
        counter.y = target.y - (target.height / 2) + (target.height / 3) * Math.sin(counter.dir);
        counter.alpha = 0.18;
        counter.speed = 2.56;
        counter.slowdown = 0.925;
        counter.fade_in = 0.04;
        counter.fade_out = 0.015;
        counter.layer = 5;

        if (options.float) {
            counter.dir = (6.14 / 360) * 270;
            counter.speed = 1.8;
            counter.x = target.x - (counter.width / 2);
            counter.y = (target.y * 1.01) - target.height;
        }

        this.counters.push(counter);
        game.stage.addChild(counter);
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
            float: true
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
            ttl: Game.secondsToTicks(2.2)
        });
    }
};