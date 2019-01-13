window.SpawnHandler = class SpawnHandler {

    constructor(camera) {
        this.camera = camera;

        server.connection.setHandler('death', event => {
            this.death(game.lookup(event.targetId), game.lookup(event.sourceId));
        });

        server.connection.setHandler('spawn', event => {
            this.handle(event);
        });

        assetLoader.load((sprite) => {
            for (let z = 0; z < 30; z++) {
                let startX = z * (sprite.width / 2);
                let startY = z * (sprite.height / 2 - 14);
                for (let i = 0; i < 30; i++) {
                    let ground = new PIXI.Sprite(PIXI.loader.resources["game/map/background_snow.png"].texture);
                    ground.x = (sprite.width / 2) * i + startX;
                    ground.y = (-sprite.height / 2 + 14) * i + startY;
                    ground.layer = -1;
                    game.stage.addChild(ground);
                }
            }
        }, "game/map/background_snow.png");

        assetLoader.load((sprite) => {
            let tree = new PIXI.Sprite(PIXI.loader.resources["game/map/tree.png"].texture);
            tree.x = 300;
            tree.y = 275;
            tree.scale.x = 0.2;
            tree.scale.y = 0.2;
            tree.layer = 0;
            game.stage.addChild(tree);

            let tree2 = new PIXI.Sprite(PIXI.loader.resources["game/map/tree.png"].texture);
            tree2.x = 375;
            tree2.y = 300;
            tree2.layer = 0;
            tree2.scale.x = 0.3;
            tree2.scale.y = 0.3;

            // if interactions available
            /*sprite.interactive = true;
            sprite.buttonMode = true;*/
            sprite.anchor.set(0.5);

            game.stage.addChild(tree2);
        }, "game/map/tree.png");

        assetLoader.begin();
    }

    handle(event) {
        let entities = [];

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
                this.despawn(game.lookup(entity.id));
            }
        }
    }

    spawn(entity) {
        let vector = entity.vector;

        if (entity.interactions.includes('loot')) {
            // no supported yet.
            return false;
        }

        assetLoader.load((sprite) => {
            Object.assign(sprite, entity);

            sprite.x = vector.x;
            sprite.y = vector.y;
            sprite.velocity = vector.velocity;
            sprite.direction = vector.direction;
            sprite.scale.x = 0.64;
            sprite.scale.y = 0.64;
            sprite.layer = 0;
            sprite.id = entity.id;
            game.entities[entity.id] = sprite;

            if (sprite.interactions.includes('dialog')) {
                sprite.interactive = true;
                sprite.buttonMode = true;

                sprite.on('pointerdown', () => {
                    game.dialogs.start(entity.id);
                });
            }

            game.stage.addChild(sprite);

            if (this.isPlayer(sprite)) {
                sprite.isPlayer = true;
                application.characterLoaded(sprite);
                this.camera.set(vector.x, vector.y);
                this.camera.focus(sprite);
            }
        }, "game/character/chr.png").begin();
    }

    isPlayer(entity) {
        return entity.account === application.token.domain;
    }

    death(target, source) {
        if (target.isPlayer) {
            game.scriptShutdown();
            application.scriptShutdown();
            application.showCharacters();
        } else {
            console.log(`kapow kapow, chat msg? at ${target.x}, {$target.y}`);
            game.texts.chat(target, `${source.name} is undone.`);
        }
    }

    despawn(entity) {
        game.stage.removeChild(entity);
        delete game.entities[entity.id];
    }
};