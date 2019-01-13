window.Particles = class Particles {

    constructor() {
        this.emitters = {};
        this.elapsed = Date.now();

        application.onGameLoaded(() => {
            this.spawn("cloud", 300, 300, 3.0);
            this.spawn("leafs", 575, 225, 12.0);
        });

        game.ticker(() => this._update());
    }


    fixed(name, target, ttl) {
        this._create((configuration) => {
            let container = new PIXI.Container();
            container.x = game.player.width;
            container.y = game.player.height / 2;
            container.layer = configuration.layer;
            configuration.emitterLifetime = ttl;
            game.player.addChildAt(container, 0);
            return container;
        }, (container) => {
            game.player.removeChild(container);
        }, name);
    }

    following(name, target, ttl) {
        this._create((configuration) => {
            let container = game.stage;
            configuration.emitterLifetime = ttl;
            //game.stage.addChild(container);
            return container;
        }, (container) => {
            game.player.removeChild(container);
        }, name, (emitter, container) => {
            emitter.updateOwnerPos(target.x, target.y);
        });
    }

    spawn(name, x, y, ttl) {
        this._create((configuration) => {
            let container = new PIXI.Container();
            container.x = x;
            container.y = y;
            container.layer = configuration.layer;
            configuration.emitterLifetime = ttl;
            game.stage.addChild(container);
            return container;
        }, (container) => {
            game.stage.removeChild(container);
        }, name);
    }

    _create(init, destroy, system, listener) {
        let images = [];

        assetLoader.load((configuration) => {
            let container = init(configuration);

            configuration.sprites.forEach(fileName => {
                assetLoader.load((sprite) => {
                    images.push(sprite.texture);

                    if (images.length === configuration.sprites.length) {
                        let emitter = new PIXI.particles.Emitter(
                            container,
                            images,
                            configuration
                        );

                        if (listener) {
                            emitter.listener = listener;
                        }

                        emitter.container = container;
                        emitter.id = Math.random().toString(36).substring(7);
                        this.emitters[emitter.id] = emitter;

                        emitter.playOnceAndDestroy(() => {
                            destroy(container);
                            delete this.emitters[emitter.id];
                        });
                    }
                }, fileName);
            });
        }, `game/particles/${system}.json`).begin();
    }

    _update() {
        let now = Date.now();

        for (let id in this.emitters) {
            let emitter = this.emitters[id];

            if (emitter.listener) {
                emitter.listener(emitter, emitter.container);
            }
            emitter.update((now - this.elapsed) * 0.001);
        }

        this.elapsed = Date.now();
    }
};