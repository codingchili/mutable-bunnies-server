const UP = 'w';
const LEFT = 'a';
const DOWN = 's';
const RIGHT = 'd';
const RUN_TOGGLE = 'z';
const PI = 3.1415927;
const ACCELERATION_BASE = 0.4;
const ACCELERATION_STEP = (1 - ACCELERATION_BASE) / Game.secondsToTicks(0.5);

window.MovementHandler = class MovementHandler {

    constructor() {
        this.run = true;
        this.last = performance.now();
        game.ticker(() => this._tick());

        input.onKeysListener({
            up: (key) => {
                this._update();
            },
            down: (key) => {
                if (key === RUN_TOGGLE) {
                    this.run = !this.run;
                }
                this._update();
            }
        }, [UP, RIGHT, LEFT, DOWN, RUN_TOGGLE]);

        window.onmousedown = (e) => {
            if (e.button === 2 && game.isPlaying) {
                game.movement.moveTo(e.x + game.camera.x, e.y + game.camera.y);
            }
        };
        window.ontouchstart = (e) => {
            if (game.isPlaying) {
                game.movement.moveTo(e.touches[0].clientX + game.camera.x, e.touches[0].clientY + game.camera.y);
            }
        };

        server.connection.setHandler('move', (event) => this._onMovement(event));
    }

    _tick() {
        let delta = performance.now() - this.last;
        for (let key in game.entities) {
            let entity = game.lookup(key);
            if (entity) {
                entity.acceleration = entity.acceleration || 1;

                if (entity.acceleration < 1.0) {
                    entity.acceleration += (ACCELERATION_STEP * (delta / Game.MS_PER_FRAME));
                } else {
                    entity.acceleration = 1.0;
                }

                if (entity.state && entity.velocity > 0) {
                    entity.state.timeScale = entity.velocity * entity.acceleration;
                }
                if (entity.state && !entity.state.initialized) {
                    entity.state.initialized = true;
                    entity.state.setAnimation(0, 'idle', true);
                }

                entity.x += Math.sin(entity.direction) * (entity.acceleration * entity.velocity) * (delta / Game.MS_PER_FRAME);
                entity.y += Math.cos(entity.direction) * (entity.acceleration * entity.velocity) * (delta / Game.MS_PER_FRAME);
            }
        }
        this.last = performance.now();
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
        return game.player.stats.movement * (this.run ? 1.0 : 0.6);
    }

    _update() {
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
                entity.state.setAnimation(0, 'walk', true);
            }

            if (event.vector.velocity === 0) {
                entity.state.setAnimation(0, 'idle', true);
            }

            if (event.vector.direction > PI || event.vector.direction < 0) {
                entity.scale.x = -entity.scale.y;
            } else if (event.vector.direction > 0 && event.vector.direction < PI) {
                entity.scale.x = entity.scale.y;
            }

            if (application.development.hardResetXY) {
                // used to check client-server delta
                entity.x = event.vector.x;
                entity.y = event.vector.y;
            }

            entity.velocity = event.vector.velocity;
            entity.direction = event.vector.direction;
        }
    }
};