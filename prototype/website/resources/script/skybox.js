/**
 * Renders the skybox at the root layer.
 */
window.Skybox = class {

    constructor() {
        this.clouds = [];
    }

    init(skybox) {
        Loader.load(background => {
            this.background = background;
            this.width = window.innerWidth;

            let ratio = Math.max(this.width / 2048, window.innerHeight / 1536);
            background.scale.x = ratio;
            background.scale.y = ratio;
            background.tint = parseInt(skybox.sky.replace('#', '0x'));

            game.root.addChildAt(background, 0);
            for (let cloud = 1; cloud <= 3; cloud++) {
                Loader.load(loaded => {
                    for (let i = 0; i < 2; i++) {
                        let cloud = new PIXI.Sprite(loaded.texture);
                        this._reset(cloud);

                        cloud.x = Math.random() * this.width;
                        cloud.tint = parseInt(skybox.clouds.replace('#', '0x'));
                        cloud.velocity = Math.random() * 42 + 16;

                        this.clouds.push(cloud);
                        game.root.addChildAt(cloud, 1);
                    }
                }, `game/map/clouds/${cloud}.png`);
            }
        }, 'game/map/clouds/skybox_grey.png');
    }

    _reset(cloud) {
        cloud.x = -cloud.width;
        cloud.y = Math.random() * window.innerHeight;
    }

    resize(width, height) {
        this.width = width;
        this.height = height;
    }

    update(delta) {
        for (let cloud of this.clouds) {
            cloud.x += cloud.velocity * delta;
            if (cloud.x - cloud.width > this.width) {
                this._reset(cloud);
            }
        }
    }
};