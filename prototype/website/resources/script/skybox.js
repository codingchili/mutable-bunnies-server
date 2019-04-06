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
                        cloud.y = Math.random() * window.innerHeight;
                        cloud.x = -cloud.width;
                        cloud.tint = parseInt(skybox.clouds.replace('#', '0x'));
                        cloud.velocity = Math.random() + 0.05;
                        this.clouds.push(cloud);
                        game.root.addChildAt(cloud, 1);
                    }
                }, `game/map/clouds/${cloud}.png`);
            }
        }, 'game/map/clouds/skybox_grey.png');
    }

    update() {
        for (let cloud of this.clouds) {
            cloud.x += cloud.velocity;
            if (cloud.x - cloud.width > window.innerWidth) {
                cloud.x = -cloud.width;
            }
        }
    }
};