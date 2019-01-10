const UP = 'w'; //38;
const LEFT = 'a'; // 37;
const DOWN = 's'; // 40;
const RIGHT = 'd'; // 39;
const ACCELERATION_START = 0.4;
const ACCELERATION_STEP = (1 - ACCELERATION_START) / Game.secondsToTicks(0.5);

window.MovementHandler = class MovementHandler {

    constructor() {
        server.connection.setHandler('move', (event) => this._onMovement(event));

        this.last = performance.now();
        game.ticker(() => this._tick());

        input.onKeysListener({
            up: (key) => {
                this._update();
            },
            down: (key) => {
                this._update();
            }
        }, [UP, RIGHT, LEFT, DOWN]);

        application.onDialogEvent(() => {
            this._send(0, 0);
        });
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

                entity.x += Math.sin(entity.direction) * (entity.acceleration * entity.velocity) * (delta / Game.MS_PER_FRAME);
                entity.y += Math.cos(entity.direction) * (entity.acceleration * entity.velocity) * (delta / Game.MS_PER_FRAME);
            }
        }
        this.last = performance.now();
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

    _update() {
        let direction = 0;
        let velocity = 0;

        if (input.isPressed([LEFT, UP, RIGHT, DOWN])) {
            // max velocity - its possible to request to move slower than max movement speed.
            velocity = 999.0;
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
        }, (event) => this._onMovement(event), );
    }

    _handle(event) {
        let entity = game.lookup(event.creatureId);

        if (entity.velocity === 0) {
            entity.acceleration = 0.4;
        }

        entity.x = event.vector.x;
        entity.y = event.vector.y;

        entity.velocity = event.vector.velocity;
        entity.direction = event.vector.direction;
    }
};