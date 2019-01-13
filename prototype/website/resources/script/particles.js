/**
 * Handles particle systems. uses pixi-particles for rendering.
 * @type {Window.Particles}
 */
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

    /**
     * Stops the given particle emitter.
     * @param id an id returned by any of the create methods.
     */
    stop(id) {
        this.emitters[id].emit = false;
        setTimeout(() => {
            this.emitters[id].destroy();
            delete this.emitters[id];
        }, 5000);
    }

    /**
     * Creates a new particle system that is fixed onto its target. Particles
     * will revolve around the given target as their 'world'.
     *
     * @param name the name of the particle system to start. matches a particle system json configuration file.
     * @param target the target to follow.
     * @param ttl the lifetime of the emitter
     * @returns {string} the ID of the created particle system so that it can be stopped.
     */
    fixed(name, target, ttl) {
        return this._create((configuration) => {
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

    /**
     * Creates a particle system where the emitter follows the given target but the particles
     * are relative to the world.
     *
     * @param name the name of the particle system to start. matches a particle system json configuration file.
     * @param target the target to follow.
     * @param ttl the lifetime of the emitter
     * @returns {string} the ID of the created particle system so that it can be stopped.
     */
    following(name, target, ttl) {
        return this._create((configuration) => {
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

    /**
     * Creates a particle system with an emitter that is fixed in the world.
     *
     * @param name the name of the particle system to start. matches a particle system json configuration file.
     * @param x coordinate of the emitter.
     * @param y coordinate ofo the emitter.
     * @param ttl the lifetime of the emitter
     * @returns {string} the ID of the created particle system so that it can be stopped.
     */
    spawn(name, x, y, ttl) {
        return this._create((configuration) => {
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
        let id = Math.random().toString(36).substring(7);
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
                        emitter.id = id;
                        this.emitters[emitter.id] = emitter;

                        emitter.playOnceAndDestroy(() => {
                            destroy(container);
                            delete this.emitters[emitter.id];
                        });
                    }
                }, fileName);
            });
        }, `game/particles/${system}.json`).begin();
        return id;
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