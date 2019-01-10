window. InputManager = class InputManager {

    constructor() {
        this.keyDownListeners = {};
        this.keyUpListeners = {};
        this.keys = {};
        this.blocked = false;

        this.onDownCallback = this._onKeyDown.bind(this);
        this.onUpCallback = this._onKeyUp.bind(this);
        // some extra variables because binding 'this' inline will produce different fn references.
        document.body.addEventListener('keydown', this.onDownCallback);
        document.body.addEventListener('keyup', this.onUpCallback);

        window.onblur = () => {
            game.movement._send(0, 0);
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
        this.keys[e.key] = false;

        if (this.keyUpListeners[e.key]) {
            for (let listener of this.keyUpListeners[e.key]) {
                if (!this.blocked) {
                    listener.up(e.key);
                }
            }
        }
    }

    _onKeyDown(e) {
        if (!this.keys[e.key]) {
            this.keys[e.key] = true;
            if (this.keyDownListeners[e.key]) {
                for (let listener of this.keyDownListeners[e.key]) {
                    if (!this.blocked) {
                        listener.down(e.key);
                    }
                }
            }
        }
    }

    shutdown() {
        document.body.removeEventListener('keydown', this.onDownCallback);
        document.body.removeEventListener('keyup', this.onUpCallback);
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