const SPAWN = 'SPAWN';
const DESPAWN = 'DESPAWN';

window.SpawnHandler = class SpawnHandler {

    constructor(camera) {
        this.camera = camera;

        server.connection.setHandler('death', event => {
            this.death(game.lookup(event.targetId), game.lookup(event.sourceId));
        });

        server.connection.setHandler('spawn', event => {
            this._spawn(event.entity, event.spawn);
        });
    }

    /**
     * Join event received when entering a new instance.
     *
     * @param event
     */
    join(event) {
        this._init(event.texture, event.size);

        // in the future we might want to split rendering of entities and creatures.
        this._spawn(event.entities, SPAWN);
        this._spawn(event.creatures, SPAWN);
    }

    _init(texture, size) {
        assetLoader.load((sprite) => {
            let container = new PIXI.Container();
            let tiling = new PIXI.extras.TilingSprite(
                sprite.texture,
                size,
                size
            );
            container.scale.y = Math.tan(0.3);
            tiling.rotation = Math.PI * 2 * (1 / 8);

            container.layer = -1;
            container.addChild(tiling);
            game.stage.addChild(container);

        }, texture);
    }

    _spawn(spawnable, type) {
        let entities = [];

        if (Array.isArray(spawnable)) {
            entities = spawnable;
        } else {
            entities.push(spawnable);
        }

        for (let entity of entities) {
            if (type === SPAWN) {
                this.spawn(entity);
            }
            if (type === DESPAWN) {
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

        assetLoader.load((jsondata) => {
            const rawSkeletonData = jsondata;
            const rawAtlasData = PIXI.loader.resources['game/pixitest/goblins-pro.json_atlas'].data;
            const spineAtlas = new PIXI.spine.core.TextureAtlas(rawAtlasData, function (image, callback) {
                image = application.realm.resources + 'game/pixitest/' + image;
                callback(PIXI.BaseTexture.fromImage(image));
            });

            const spineAtlasLoader = new PIXI.spine.core.AtlasAttachmentLoader(spineAtlas);
            const spineJsonParser = new PIXI.spine.core.SkeletonJson(spineAtlasLoader);

            spineJsonParser.scale = 2.0;

            const spineData = spineJsonParser.readSkeletonData(rawSkeletonData);
            const sprite = new PIXI.spine.Spine(spineData);

            Object.assign(sprite, entity);

            sprite.skeleton.setSkinByName('goblingirl');

            sprite.x = vector.x;
            sprite.y = vector.y;
            sprite.velocity = vector.velocity;
            sprite.direction = vector.direction;
            sprite.scale.set(0.16);
            sprite.layer = 1;
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
                game.setPlayer(sprite);
                this.camera.set(vector.x, vector.y);
                this.camera.focus(sprite);
            }
        }, "game/pixitest/goblins-pro.json").begin();
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
            game.chat.add({'text': `${target.name} was undone by ${source.name}`, 'source': target.id});
        }
    }

    despawn(entity) {
        game.stage.removeChild(entity);
        delete game.entities[entity.id];
    }
};