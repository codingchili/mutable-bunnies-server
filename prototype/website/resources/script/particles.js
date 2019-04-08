/**
 * Handles particle systems. uses pixi-particles for rendering.
 * @type {Window.Particles}
 */
window.Particles = class Particles {

    constructor() {
        this.emitters = {};
        this.elapsed = Date.now();
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
            container.layer = configuration.layer;
            configuration.emitterLifetime = ttl;
            target.addChild(container);
            return container;
        }, (container) => {
            target.removeChild(container);
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
            let container = new PIXI.Container();
            configuration.emitterLifetime = ttl;
            container.layer = configuration.layer;
            game.stage.addChild(container);
            return container;
        }, (container) => {
            game.stage.removeChild(container);
        }, name, (emitter, container) => {
            emitter.updateOwnerPos(target.x, target.y);
        });
    }

    /**
     * @param name the name of the particle system to spawn.
     * @param start the starting position x,y
     * @param options destination x,y and velocity.
     * @returns {string} the ID of the created particle system so that it can be stopped.
     */
    moving(name, start, options) {
        let destination = options.destination;
        let distance = Math.hypot(Math.abs(start.x - destination.x), Math.abs(destination.y - start.y));
        let ttl = Game.ticksToSeconds(distance / options.velocity);

        return this._create((configuration) => {
            let container = new PIXI.Container();
            configuration.emitterLifetime = ttl;

            container.layer = configuration.layer;
            container.x = start.x;
            container.y = start.y;
            //container.direction = direction;
            container.velocity = options.velocity;
            container.acceleration = 1.0;

            game.stage.addChild(container);
            return container;
        }, (container) => {
            game.stage.removeChild(container);
            if (options.complete) {
                options.complete();
            }
        }, name, (emitter, container, delta) => {
            container.direction = (Math.atan2(container.y - destination.y, destination.x - container.x));
            container.direction += 90 * (Math.PI / 180);

            // updateOwnerPos vs. update container pos.
            emitter.ownerPos.x += Math.sin(container.direction) * (container.acceleration * container.velocity) * (delta / Game.MS_PER_FRAME);
            emitter.ownerPos.y += Math.cos(container.direction) * (container.acceleration * container.velocity) * (delta / Game.MS_PER_FRAME);
            emitter.posChanged = true;
        });
    }

    /**
     * Creates a particle system with an emitter that is fixed in the world.
     *
     * @param name the name of the particle system to start. matches a particle system json configuration file.
     * @param target coordinate of the emitter.
     * @param ttl the lifetime of the emitter
     * @returns {string} the ID of the created particle system so that it can be stopped.
     */
    spawn(name, target, ttl) {
        return this._create((configuration) => {
            let container = new PIXI.Container();
            container.x = target.x;
            container.y = target.y;
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

        Loader.load((configuration) => {
            let container = init(configuration);
            container.particles = true;

            configuration.sprites.forEach(fileName => {
                Loader.load((sprite) => {
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
                        this.emitters[id] = emitter;

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

    update(delta) {
        for (let id in this.emitters) {
            let emitter = this.emitters[id];

            if (emitter.listener) {
                emitter.listener(emitter, emitter.container, delta);
            }
            emitter.update(delta * 0.001);
        }
    }
};