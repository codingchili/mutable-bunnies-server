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
        game.entities = [];

        this._init(event.texture, event.size);

        // in the future we might want to split rendering of entities and creatures.
        this._spawn(event.entities, SPAWN);
        this._spawn(event.creatures, SPAWN);
    }

    _init(texture, size) {
        Loader.load((sprite) => {
            let container = new PIXI.Container();
            let tiling = new PIXI.TilingSprite(
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
        let animated = this._isAnimated(entity);

        Loader.load((resource) => {
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
            this._onContextMenuHook(sprite);
            this._onTargetHook(sprite);
            this._processAfflictionsHook(sprite);

            if (entity.account) {
                game.chat.add({text: `${entity.name} has joined.`, system: true});
            }

            game.stage.addChild(sprite);

        }, this._graphicsToUrl(entity.model, animated)).begin();
    }

    _isAnimated(entity) {
        let graphics = entity.model.graphics;
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
            resource.scale.x = entity.model.scale;
            resource.scale.y = entity.model.scale;
            return resource;
        }
    }

    _parseSpineData(jsondata, entity) {
        const rawSkeletonData = jsondata;
        const rawAtlasData = Loader.resources[`${entity.model.graphics}.json_atlas`].data;
        const spineAtlas = new PIXI.spine.core.TextureAtlas(rawAtlasData, (image, callback) => {
            callback(PIXI.BaseTexture.from(this._getImageNameFrom(entity.model.graphics, image)));
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

    _onLootableHook(entity) {
        if (entity.interactions.includes('loot')) {
            entity.interactive = true;
            entity.buttonMode = true;

            entity.on('pointerdown', (e) => {
                input.ifLeftMouse(() => {
                    game.inventory.requestLootList(entity);
                });
            });
        }
    }

    _onContextMenuHook(entity) {
        entity.interactive = true;
        entity.on('pointerdown', (e) => {
            input.ifRightMouse(() => {
                if (e.data.originalEvent.altKey) {
                    application.publish('context-menu', {
                        pointer: e,
                        target: entity
                    });
                }
            });
        });
    }

    _onDialogSupportedHook(entity) {
        if (entity.interactions.includes('dialog')) {
            entity.interactive = true;
            entity.buttonMode = true;
            entity.on('pointerdown', (e) => {
                input.ifLeftMouse(() => {
                    game.dialogs.start(entity.id);
                });
            });
        }
    }

    _onTargetHook(entity) {
        entity.interactive = true;
        entity.on('pointerdown', (e) => {
            input.ifLeftMouse(() => {
                game.publish('character-target', entity);
            });
        });
    }

    _onPlayerSpawnHook(sprite) {
        if (this.isPlayer(sprite)) {
            sprite.isPlayer = true;
            game.setPlayer(sprite);
            application.characterLoaded(sprite);
            game.publish('character-update', sprite);
            this.camera.set(sprite.vector.x, sprite.vector.y);
            this.camera.focus(sprite);
        }
    }

    _processAfflictionsHook(sprite) {
        if (sprite.afflictions) {
            for (let item of sprite.afflictions) {
                item.loaded = true;
                game.spells.affliction(item);
            }
        }
    }

    _getImageNameFrom(path, imageName) {
        let directoryName = path.substr(0, path.lastIndexOf('/'));
        return `${application.realm.resources}${directoryName}/${imageName}`;
    }

    isPlayer(entity) {
        return entity.account === application.token.domain;
    }

    death(target, source) {
        target.dead = true;

        if (target.isPlayer) {
            sound.play("the_end.mp3");

            application.publish('player-death', () => {
                game.shutdown();
                application.showCharacters();
            });
        } else {
            sound.play("drop_dead.mp3");
            if (target.account) {
                game.chat.add({text: `${target.name} was undone by ${source.name}.`, system: true});
            }
        }
    }

    despawn(target) {
        game.stage.removeChild(target);
        delete game.entities[target.id];

        if (target.account && !target.dead) {
            game.chat.add({text: `${target.name} has left.`, system: true});
        }
    }
};