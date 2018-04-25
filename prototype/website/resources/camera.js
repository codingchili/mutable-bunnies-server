window.Camera = class Camera {

    // should follow projectiles.
    // could follow other players.
    // implement some screen shake.

    constructor() {
        this.player = {};
        this.x = 500;
        this.y = 500;
        this.player.x = 0;
        this.player.y = 0;

        let last = performance.now();

        game.ticker(() => {
            let delta = performance.now() - last;
            // camera smoothing etc.
            let target = this._getTarget(this.player.x, this.player.y);

            let deltaX = (this.x - target.x);
            let deltaY = (this.y - target.y);

            this.x -= deltaX * 0.064 * (delta / Game.MS_PER_FRAME);
            this.y -= deltaY * 0.064 * (delta / Game.MS_PER_FRAME);

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
        let player = this.player;
        this.player = {x: player.x, y: player.y};
        this.player.x += 300;
        setTimeout(() => {
            this.player.x = player.x - 300;
            setTimeout(() => {
                this.player.x = player.x + 300;
                setTimeout(() => {
                    this.player = player;
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
        let target = this._getTarget(x, y);
        this.y = target.y;
        this.x = target.x;
    }

    _getTarget(x, y) {
        let target = {};
        target.x = x - Math.round(window.innerWidth / 2);
        target.y = y - Math.round(window.innerHeight / 2);
        return target;
    }

    focus(player) {
        this.player = player;
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
}