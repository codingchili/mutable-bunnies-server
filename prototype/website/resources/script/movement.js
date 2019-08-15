const UP = 'w';
const LEFT = 'a';
const DOWN = 's';
const RIGHT = 'd';
const RUN_TOGGLE = 'z';
const WALKING_SPEED = 105.0; // constant to make all bunnies walk at the same speed.
const PI = 3.1415927;
const ACCELERATION_BASE = 0.4;
const ACCELERATION_STEP = (1 - ACCELERATION_BASE);
const ACCELERATION_TIME = 0.84;

window.MovementHandler = class MovementHandler {

    constructor() {
        this.run = true;
        this.last = performance.now();

        application.subscribe('player-death', () => {
            for (let key in game.entities) {
                let entity = game.lookup(key);
                if (entity) {
                    entity.velocity = 0;
                    this._setAnimation(game.player);
                }
            }
        });

        input.onKeysListener({
            up: (key) => {
                this._input();
            },
            down: (key) => {
                if (key === RUN_TOGGLE) {
                    this.run = !this.run;
                    this._setAnimation(game.player);
                }
                this._input();
            }
        }, [UP, RIGHT, LEFT, DOWN, RUN_TOGGLE]);

        window.onmousedown = (e) => {
            if (e.button === 2 && game.isPlaying && !e.altKey && !input.blocked) {
                game.movement.moveTo(e.x + game.camera.x, e.y + game.camera.y);
            }
        };
        window.ontouchstart = (e) => {
            if (game.isPlaying && !input.blocked) {
                game.movement.moveTo(e.touches[0].clientX + game.camera.x, e.touches[0].clientY + game.camera.y);
            }
        };
        server.connection.setHandler('move', (event) => this._onMovement(event));
    }

    update(delta) {
        for (let key in game.entities) {
            let entity = game.lookup(key);
            if (entity) {
                entity.acceleration = entity.acceleration || 1;

                if (entity.acceleration < 1.0) {
                    entity.acceleration += ACCELERATION_STEP * (delta / ACCELERATION_TIME);
                } else {
                    entity.acceleration = 1.0;
                }

                if (entity.state && entity.velocity > 0) {
                    entity.state.timeScale = entity.velocity * entity.acceleration * delta * 1.4;
                }
                if (entity.state && !entity.state.initialized) {
                    entity.state.initialized = true;
                    this._setAnimation(entity);
                }

                entity.x += Math.sin(entity.direction) * (entity.acceleration * entity.velocity) * delta;
                entity.y += Math.cos(entity.direction) * (entity.acceleration * entity.velocity) * delta;
            }
        }
    }

    moveTo(x, y) {
        server.connection.send('moveTo', {
            vector: {
                x: x,
                y: y,
                velocity: this._velocity()
            }
        }, (event) => this._onMovement(event))
    }

    _onMovement(event) {
        if (Array.isArray(event.spawn)) {
            for (let i in event.spawn) {
                this._handle(event.spawn[i]);
            }
        } else {
            this._handle(event);
        }
    }

    _velocity() {
        if (this.run) {
            return game.player.stats.movement;
        } else {
            return WALKING_SPEED;
        }
    }

    _input() {
        let direction = 0;
        let velocity = 0;
        let max = this._velocity();

        if (input.isPressed([LEFT, UP, RIGHT, DOWN])) {
            velocity = max;
        }

        if (input.isPressed(LEFT)) {
            direction = 270;
            if (input.isPressed(DOWN)) {
                direction += 30;
            }
            if (input.isPressed(UP)) {
                direction -= 30;
            }
        } else if (input.isPressed(UP)) {
            direction = 180;
            if (input.isPressed(RIGHT)) {
                direction -= 60;
            }
        } else if (input.isPressed(RIGHT)) {
            direction = 90;
            if (input.isPressed(DOWN)) {
                direction -= 30;
            }
        } else if (input.isPressed(DOWN)) {
            direction = 0;
        }

        direction = direction * Math.PI / 180;
        this._send(direction, velocity);
    }

    _send(direction, velocity) {
        server.connection.send('move', {
            vector: {
                direction: direction,
                velocity: velocity
            },
        }, (event) => this._onMovement(event),);
    }

    _handle(event) {
        let entity = game.lookup(event.creatureId);

        if (entity) {
            if (entity.velocity === 0) {
                entity.acceleration = ACCELERATION_BASE;
            }

            if (event.vector.direction > PI || event.vector.direction < 0) {
                entity.scale.x = (entity.model.revertX) ? entity.scale.y : -entity.scale.y;
            } else if (event.vector.direction > 0 && event.vector.direction < PI) {
                entity.scale.x = (entity.model.revertX) ? -entity.scale.y : entity.scale.y;
            }

            if (application.development.hardResetXY) {
                // used to check client-server delta
                entity.x = event.vector.x;
                entity.y = event.vector.y;
            }

            entity.velocity = event.vector.velocity;
            entity.direction = event.vector.direction;

            this._setAnimation(entity);
        }
    }

    _setAnimation(entity) {
        let animation = (entity.velocity > WALKING_SPEED) ? 'run' : 'walk';

        if (entity.velocity === 0) {
            animation = 'idle';
        }

        if (entity.state.initialized) {
            if (entity.state.tracks.length === 0 || animation !== entity.state.tracks[0].animation.name) {
                entity.state.setAnimation(0, animation, true);
            }
        }
    }
};