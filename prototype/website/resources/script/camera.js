window.Camera = class Camera {

    constructor() {
        this.smoothing = 6.4;
        this.clipping = 4;
        this.x = -2000;
        this.y = -2000;
        this.following = {x: this.x, y: this.y};
        this.drawing = 0;
    }

    update(delta) {
        let target = this._getTarget(this.following.x, this.following.y);

        let deltaX = (this.x - target.x);
        let deltaY = (this.y - target.y);

        if (Math.abs(deltaX) > this.clipping) {
            this.x -= deltaX * (this.smoothing * delta);
        }

        if (Math.abs(deltaY) > this.clipping) {
            this.y -= deltaY * (this.smoothing * delta);
        }

        this.cull(game.stage.children);
    }

    static depthCompare(a, b) {
        if (a.visible && b.visible) {

            if (a.layer !== b.layer) {
                if ((a.layer > b.layer)) {
                    return 1;
                } else {
                    return -1;
                }
            }
            if (a.y === b.y) {
                return (a.x < b.x) ? 1 : -1;
            } else {
                return (a.y < b.y) ? -1 : 1;
            }
        }
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
        let boundY = this.y + window.innerHeight;
        let boundX = this.x + window.innerWidth;
        this.drawing = 0;

        // cull all sprites that are fully outside of the screen.
        for (let sprite of sprites) {
            let x = sprite.x;
            let y = sprite.y;
            let visible = false;

            if (sprite.particles) {
                // don't cull particle systems; no access to emitters ownerPos here.
                sprite.visible = true;
            } else {
                // left and right.
                if (x + sprite.width > this.x && x - sprite.width < boundX) {
                    // top and bottom.
                    if (y + sprite.height / 2 > this.y && y - sprite.height < boundY) {
                        visible = true;
                    }
                }

                if (visible) {
                    this.drawing++;
                }
                sprite.visible = visible || sprite.layer === -1;
            }
        }
    }
};