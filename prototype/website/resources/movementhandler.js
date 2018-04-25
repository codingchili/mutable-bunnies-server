const UP = 87; //38;
const LEFT = 65; // 37;
const DOWN = 83; // 40;
const RIGHT = 68; // 39;

window.MovementHandler = class MovementHandler {

    constructor() {
        server.connection.setHandler('move', (event) => this._onMovement(event));

        this.last = performance.now();
        game.ticker(() => this._update());

        inputManager.onKeysListener({
            up: (key) => {
                this._sendUpdate();
            },
            down: (key) => {
                this._sendUpdate();
            }
        }, [UP, LEFT, DOWN, RIGHT]);
    }

    _update() {
        let delta = performance.now() - this.last;
        for (let key in game.entities) {
            let entity = game.lookup(key);
            if (entity) {
                entity.x += Math.sin(entity.direction) * entity.velocity * (delta / Game.MS_PER_FRAME);
                entity.y += Math.cos(entity.direction) * entity.velocity * (delta / Game.MS_PER_FRAME);
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

    _sendUpdate() {
        let direction = 0;
        let velocity = 0;

        if (inputManager.isPressed([LEFT, UP, RIGHT, DOWN])) {
            // max velocity - its possible to request to move slower than max movement speed.
            velocity = 999.0;
        }

        if (inputManager.isPressed(LEFT)) {
            direction = 270;
            if (inputManager.isPressed(DOWN)) {
                direction += 30;
            }
            if (inputManager.isPressed(UP)) {
                direction -= 30;
            }
        } else if (inputManager.isPressed(UP)) {
            direction = 180;
            if (inputManager.isPressed(RIGHT)) {
                direction -= 60;
            }
        } else if (inputManager.isPressed(RIGHT)) {
            direction = 90;
            if (inputManager.isPressed(DOWN)) {
                direction -= 30;
            }
        } else if (inputManager.isPressed(DOWN)) {
            direction = 0;
        }

        direction = direction * Math.PI / 180;

        server.connection.send((event) => this._onMovement(event), 'move', {
            vector: {
                direction: direction,
                velocity: velocity
            }
        });
    }

    _handle(event) {
        let entity = game.lookup(event.creatureId);
        entity.x = event.vector.x;
        entity.y = event.vector.y;
        entity.velocity = event.vector.velocity;
        entity.direction = event.vector.direction;
    }
}