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

            let ratio = Math.max(window.innerWidth / 2048, window.innerHeight / 1536);
            background.scale.x = ratio;
            background.scale.y = ratio;
            background.tint = parseInt(skybox.sky.replace('#', '0x'));

            game.root.addChildAt(background, 0);
            for (let cloud = 1; cloud <= 3; cloud++) {
                Loader.load(loaded => {
                    for (let i = 0; i < 2; i++) {
                        let cloud = new PIXI.Sprite(loaded.texture);
                        this._reset(cloud);

                        cloud.x = Math.random() * window.innerWidth;
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
        //let scale = (cloud.velocity / 68); // large velocity = larger
        //cloud.scale.x = scale;
        //cloud.scale.y = scale;
        //cloud.layer = (-1) - scale;
        cloud.x = -cloud.width;
        cloud.y = Math.random() * window.innerHeight;
    }

    update(delta) {
        for (let cloud of this.clouds) {
            cloud.x += cloud.velocity * delta;
            if (cloud.x - cloud.width > window.innerWidth) {
                this._reset(cloud);
            }
        }
    }
};