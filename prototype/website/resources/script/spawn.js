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
            container.scale.y = Math.tan(30 * Math.PI / 180);
            tiling.rotation += Math.PI * 2 * (1 / 8);

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
        let animated = this._isAnimated(entity.model.graphics);

        assetLoader.load((resource) => {
            let sprite = this._loadSpriteFrom(resource, entity, animated);

            Object.assign(sprite, entity);

            sprite.x = vector.x;
            sprite.y = vector.y;
            sprite.velocity = vector.velocity;
            sprite.direction = vector.direction;
            sprite.layer = entity.model.layer;
            sprite.id = entity.id;

            game.entities[entity.id] = sprite;

            this._onPlayerSpawnHook(sprite);
            this._onDialogSupportedHook(sprite);
            this._onLootableHook(sprite);
            this._onDescriptionHook(sprite);

            game.stage.addChild(sprite);

        }, this._graphicsToUrl(entity.model, animated)).begin();
    }

    _isAnimated(graphics) {
        return !(graphics.includes('.png') || graphics.includes('.jpg'));
    }

    _loadSpriteFrom(resource, entity, animated) {
        if (animated) {
            let sprite = new PIXI.spine.Spine(this._parseSpineData(resource, entity));
            if (entity.model.skin) {
                sprite.skeleton.setSkinByName(entity.model.skin);
            }
            return sprite;
        } else {
            // the resource is already converted to a sprite by the loader.
            resource.pivot.y = resource.height - 50; // todo: apply bounding box!
            resource.pivot.x = resource.width / 2;
            return resource;
        }
    }

    _parseSpineData(jsondata, entity) {
        const rawSkeletonData = jsondata;
        const rawAtlasData = PIXI.loader.resources[`${entity.model.graphics}.json_atlas`].data;
        const spineAtlas = new PIXI.spine.core.TextureAtlas(rawAtlasData, (image, callback) => {
            callback(PIXI.BaseTexture.fromImage(this._getImageNameFrom(entity.model.graphics, image)));
        });

        const spineAtlasLoader = new PIXI.spine.core.AtlasAttachmentLoader(spineAtlas);
        const spineJsonParser = new PIXI.spine.core.SkeletonJson(spineAtlasLoader);

        spineJsonParser.scale = entity.model.scale;

        return spineJsonParser.readSkeletonData(rawSkeletonData);
    }

    _graphicsToUrl(model, animated) {
        if (animated) {
            return `${model.graphics}.json`;
        } else {
            return model.graphics;
        }
    }

    _onLootableHook(sprite) {
        if (sprite.interactions.includes('loot')) {
            // no supported yet.
            return false;
        }
    }

    _onDescriptionHook(sprite) {
        let description = sprite.attributes['description'] || sprite.name;

        if (description) {
            sprite.interactive = true;
            sprite.on('pointerdown', (e) => {
                input.ifRightMouse(() => {
                    game.texts.chat(sprite, {text: description});
                });
            });
        }
    }

    _onDialogSupportedHook(sprite) {
        if (sprite.interactions.includes('dialog')) {
            sprite.interactive = true;
            sprite.buttonMode = true;
            sprite.on('pointerdown', (e) => {
                input.ifLeftMouse(() => {
                    game.dialogs.start(sprite.id);
                });
            });
        }
    }

    _onPlayerSpawnHook(sprite) {
        if (this.isPlayer(sprite)) {
            sprite.isPlayer = true;
            application.characterLoaded(sprite);
            game.setPlayer(sprite);
            this.camera.set(sprite.vector.x, sprite.vector.y);
            this.camera.focus(sprite);
        }
    }

    _getImageNameFrom(path, imageName) {
        let directoryName = path.substr(0, path.lastIndexOf('/'));
        return `${application.realm.resources}/${directoryName}/${imageName}`;
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