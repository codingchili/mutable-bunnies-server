class InputManager {

    constructor() {
        this.keyDownListeners = {};
        this.keyUpListeners = {};
        this.keys = {};


        this.onDownCallback = this._onKeyDown.bind(this);
        this.onUpCallback = this._onKeyUp.bind(this);
        // some extra variables because binding 'this' inline will produce different fn references.
        document.body.addEventListener('keydown', this.onDownCallback);
        document.body.addEventListener('keyup', this.onUpCallback);
    }

    onKeysListener(callback, keys) {
        for (let key of keys) {
            this.keyUpListeners[key] = this.keyUpListeners[key] || [];
            let listeners = this.keyUpListeners[key];
            listeners.push(callback);

            this.keyDownListeners[key] = this.keyDownListeners[key] || [];
            listeners = this.keyDownListeners[key];
            listeners.push(callback);
        }
    }

    _onKeyUp(e) {
        this.keys[e.keyCode] = false;

        if (this.keyUpListeners[e.keyCode]) {
            for (let listener of this.keyUpListeners[e.keyCode]) {
                listener.up(e.keyCode);
            }
        }
    }

    _onKeyDown(e) {
        if (!this.keys[e.keyCode]) {
            this.keys[e.keyCode] = true;
            if (this.keyUpListeners[e.keyCode]) {
                for (let listener of this.keyDownListeners[e.keyCode]) {
                    listener.down(e.keyCode);
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
        return pressed;
    }

}

var inputManager = new InputManager();