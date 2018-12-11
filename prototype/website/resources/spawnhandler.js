window.SpawnHandler = class SpawnHandler {

    constructor(camera) {
        this.camera = camera;

        server.connection.setHandler('spawn', event => {
            this.handle(event);
        });

        assetLoader.load((sprite) => {
            for (let z = 0; z < 30; z++) {
                let startX = z * (sprite.width / 2);
                let startY = z * (sprite.height / 2 - 14);
                for (let i = 0; i < 30; i++) {
                    let ground = new PIXI.Sprite(PIXI.loader.resources["background_snow.png"].texture);
                    ground.x = (sprite.width / 2) * i + startX;
                    ground.y = (-sprite.height / 2 + 14) * i + startY;
                    ground.layer = -1;
                    game.stage.addChild(ground);
                }
            }
        }, "background_snow.png");

        assetLoader.load((sprite) => {
            let tree = new PIXI.Sprite(PIXI.loader.resources["tree.png"].texture);
            tree.x = 300;
            tree.y = 275;
            tree.scale.x = 0.2;
            tree.scale.y = 0.2;
            tree.layer = 0;
            game.stage.addChild(tree);

            let tree2 = new PIXI.Sprite(PIXI.loader.resources["tree.png"].texture);
            tree2.x = 375;
            tree2.y = 300;
            tree2.layer = 0;
            tree2.scale.x = 0.3;
            tree2.scale.y = 0.3;
            game.stage.addChild(tree2);
        }, "tree.png");

        assetLoader.begin();
    }

    handle(event) {
        let entities = [];

        console.log(event);

        if (Array.isArray(event.entities)) {
            entities = event.entities;
        } else {
            entities.push(event.entities);
        }

        for (let entity of entities) {
            if (event.spawn === 'SPAWN') {
                this.spawn(entity);
            }
            if (event.spawn === 'DESPAWN') {
                this.despawn(entity);
            }
        }
    }

    spawn(entity) {
        let vector = entity.vector;

        assetLoader.load((sprite) => {
            Object.assign(sprite, entity);
            console.log('post-assign');
            console.log(entity);
            console.log(sprite);
            sprite.x = vector.x;
            sprite.y = vector.y;
            sprite.velocity = vector.velocity;
            sprite.direction = vector.direction;
            sprite.scale.x = 0.64;
            sprite.scale.y = 0.64;
            sprite.layer = 0;
            sprite.id = entity.id;
            game.entities[entity.id] = sprite;
            game.stage.addChild(sprite);

            if (this.isPlayer(sprite)) {
                sprite.isPlayer = true;
                application.characterLoaded(sprite);
                this.camera.set(vector.x, vector.y);
                this.camera.focus(sprite);
            }

        }, "chr.png").begin();
    }

    isPlayer(entity) {
        return entity.account === application.token.domain;
    }

    despawn(entity) {
        entity = game.lookup(entity.id);
        game.stage.removeChild(entity);
        game.entities[entity.id] = null;
    }
};