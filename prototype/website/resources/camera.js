window.Camera = class Camera {

    constructor() {
        this.smoothing = 0.064;
        this.clipping = 4;
        this.x = -2000;
        this.y = -2000;
        this.following = {x: this.x, y: this.y};

        let last = performance.now();

        game.ticker(() => {
            let delta = performance.now() - last;
            let target = this._getTarget(this.following.x, this.following.y);

            let deltaX = (this.x - target.x);
            let deltaY = (this.y - target.y);

            if (Math.abs(deltaX) > this.clipping) {
                this.x -= deltaX * (this.smoothing * (delta / Game.MS_PER_FRAME));
            }

            if (Math.abs(deltaY) > this.clipping) {
                this.y -= deltaY * (this.smoothing * (delta / Game.MS_PER_FRAME));
            }

            this.cull(game.stage.children);
            last = performance.now();
        });
    }

    getX() {
        return -this.x;
    }

    getY() {
        return -this.y;
    }

    shake() {
        let start = this.following;
        this.following = {x: start.x + 300, y: start.y};

        setTimeout(() => {
            this.following.x = start.x - 300;
            setTimeout(() => {
                this.following.x = start.x + 300;
                setTimeout(() => {
                    this.following = start;
                }, 165);
            }, 165);
        }, 165);
    }

    /**
     * Sets the camera to the given position without smoothing.
     * @param x the x coordinate of the object to focus.
     * @param y the y coordinate of the object to focus.
     */
    set(x, y) {
        this.following = this._getTarget(x, y);
        this.y = this.following.y;
        this.x = this.following.x;
    }

    _getTarget(x, y) {
        let target = {};
        target.x = x - Math.round(window.innerWidth / 2);
        target.y = y - Math.round(window.innerHeight / 2);
        return target;
    }

    focus(target) {
        this.following = target;
    }

    cull(sprites) {
        let x = this.x;
        let y = this.y;
        let boundY = y + window.innerHeight;
        let boundX = x + window.innerWidth;
        let drawing = 0;

        // cull all sprites that are fully outside of the screen.
        for (let sprite of sprites) {
            let visible = false;

            // left and right.
            if (sprite.x + sprite.width > x && sprite.x < boundX) {
                // top and bottom.
                if (sprite.y + sprite.height > y && sprite.y < boundY) {
                    visible = true;
                }
            }

            if (visible) {
                drawing++;
            }
            sprite.visible = visible;
        }
    }
};