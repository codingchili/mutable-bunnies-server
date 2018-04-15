var game;

window.onScriptsLoaded = () => {
    game = new Game();

    server.join({
        accepted: (resp) => {
            console.log(resp);
        },
        error: (resp) => {
            application.error(resp.message);
        }
    }, character.name);
}

window.onScriptShutdown = () => {
    if (game) {
        game.shutdown();
    }
    server.leave();
}

class Game extends Canvas {

    depthCompare(a, b) {
            if (a.layer !== b.layer) {
                if ((a.layer > b.layer))
                    return 1;
                else
                    return -1;
            }
            let yB = b.y + b.height;
            let yA = a.y + a.height;

            if (yA < yB)
                return -1;
            else if (yA > yB)
                return 1;
            else
                return 0;
        }

    shutdown() {
        super.shutdown();
        this.isPlaying = false;
    }

    constructor() {
        super();
        this.entities = {};
        this.isPlaying = true;

        assetLoader.load((sprite) => {
            let ground = new PIXI.Sprite(PIXI.loader.resources["background_snow.png"].texture);
                            ground.x = 50;
                            ground.y = -100;
                            ground.layer = -1;
                            this.stage.addChild(ground);
        }, "background_snow.png");

        assetLoader.load((sprite) => {
          let tree = new PIXI.Sprite(PIXI.loader.resources["tree.png"].texture);
            tree.x = 300;
            tree.y = 275;
            tree.scale.x = 0.2;
            tree.scale.y = 0.2;
            tree.layer = 0;
            this.stage.addChild(tree);

            let tree2 = new PIXI.Sprite(PIXI.loader.resources["tree.png"].texture);
            tree2.x = 375;
            tree2.y = 300;
            tree2.layer = 0;
            tree2.scale.x = 0.3;
            tree2.scale.y = 0.3;
            this.stage.addChild(tree2);
        }, "tree.png");

        assetLoader.begin();

      this.app.ticker.add(() => {
            let player = this.lookup(character.id);

            if (player) {
                if (this.keys[37])
                    player.x -= 1;
                if (this.keys[38])
                    player.y -= 1;
                if (this.keys[39])
                    player.x += 1;
                if (this.keys[40])
                    player.y += 1;
            }
        });

        //Start the game loop
        this.loop();
    }

    lookup(id) {
        return this.entities[id];
    }

    loop() {
            this.stage.children.sort(this.depthCompare);

            textEffects.update();

            //Render the stage to see the animation
            this.renderer.render(this.stage);

            //Loop this function at 60 frames per second
            if (this.isPlaying) {
                requestAnimationFrame(() => this.loop());
            }
        }
}