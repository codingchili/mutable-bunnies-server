class SpawnHandler {

    constructor() {
        server.connection.setHandler('SPAWN', event => {

            if (Array.isArray(event.spawn)) {
                for (let i in event.spawn) {
                    this.handle(event.spawn[i]);
                }
            } else {
                this.handle(event);
            }
         });
    }

    handle(event) {
        if (event.spawn == 'SPAWN') {
            this.spawn(event.entity);
        }
        if (event.spawn == 'DESPAWN') {
            this.despawn(event.entity);
        }
    }

    spawn(entity) {
        assetLoader.load((sprite) => {
            let vector = entity.vector;
            vector.x = 475;
            vector.y = 245;
            sprite.x = vector.x;
            sprite.y = vector.y;
            sprite.scale.x = 0.64;
            sprite.scale.y = 0.64;
            sprite.id = entity.id;
            sprite.layer = 0;
            game.entities[entity.id] = sprite;
            game.stage.addChild(sprite);
            // for now: always assume its a player who spawns.
            // in the future this can be anything including nodes, strucutres nps.
            // chr.png should be more complex: and made up of multiple parts and
            // programmatically animated.
        }, "chr.png").begin();
    }

    despawn(entity) {
        entity = game.lookup(entity.id);
        game.stage.removeChild(entity);
        game.entities[entity.id] = null;
    }
}

var spawnHandler = new SpawnHandler();