const MOUSE_LEFT = 0;
const MOUSE_RIGHT = 2;

window.InputManager = class InputManager {

    constructor() {
        this.ESCAPE = 'Escape';
        this.LMB = 0;
        this.RMB = 2;

        this.keyDownListeners = {};
        this.keyUpListeners = {};
        this.keys = {};
        this.blocked = false;

        this.onDownCallback = this._onKeyDown.bind(this);
        this.onUpCallback = this._onKeyUp.bind(this);
        // some extra variables because binding 'this' inline will produce different fn references.
        document.body.addEventListener('keydown', this.onDownCallback);
        document.body.addEventListener('keyup', this.onUpCallback);
        document.body.addEventListener('mousedown', this.onDownCallback);
        document.body.addEventListener('mouseup', this.onUpCallback);

        window.onblur = () => {
            if (game.isPlaying && !game.player.dead) {
                game.movement._send(0, 0);
            }
            this.keys = {};
        };
    }

    onKeysListener(callback, keys) {
        for (let key of keys) {
            if (callback.up) {
                this.keyUpListeners[key] = this.keyUpListeners[key] || [];
                this.keyUpListeners[key].push(callback);
            }

            if (callback.down) {
                this.keyDownListeners[key] = this.keyDownListeners[key] || [];
                this.keyDownListeners[key].push(callback);
            }
        }
    }

    block() {
        this.blocked = true;
    }

    unblock() {
        this.blocked = false;
    }

    _onKeyUp(e) {
        let key = e.key || e.button;

        this.keys[key] = false;
        if (this.keyUpListeners[key]) {
            for (let listener of this.keyUpListeners[key]) {
                if (!this.blocked) {
                    listener.up(key);
                }
            }
        }
    }

    ifLeftMouse(callback) {
        setTimeout(() => {
            if (this.keys[MOUSE_LEFT]) {
                callback()
            }
        }, 1);
    }

    ifRightMouse(callback) {
        setTimeout(() => {
            if (this.keys[MOUSE_RIGHT]) {
                callback()
            }
        }, 1);
    }

    _onKeyDown(e) {
        let key = e.key || e.button;
        if (!this.keys[key]) {
            this.keys[key] = true;
            if (this.keyDownListeners[key]) {
                for (let listener of this.keyDownListeners[key]) {
                    if (!this.blocked) {
                        listener.down(key);
                    }
                }
            }
        }
    }

    shutdown() {
        document.body.removeEventListener('keydown', this.onDownCallback);
        document.body.removeEventListener('keyup', this.onUpCallback);
        document.body.removeEventListener('mousedown', this.onUpCallback);
        document.body.removeEventListener('mouseup', this.onUpCallback);
    }

    isPressed(keys) {
        let pressed = false;
        if (Array.isArray(keys)) {
            for (let key of keys) {
                pressed |= this.keys[key];
            }
        } else {
            pressed = this.keys[keys];
        }
        return !(this.blocked) && pressed;
    }

};

var input = new InputManager();