window.Game = class Game extends Canvas {

    static ticksToSeconds(ticks) {
        return (ticks * this.MS_PER_FRAME) / 1000.0;
    }

    static secondsToTicks(seconds) {
        return (seconds * 1000) / this.MS_PER_FRAME;
    }

    static get MS_PER_FRAME() {
        return 16;
    }

    onScriptsLoaded(done) {
        game.init();

        console.log('init game');

        server.join({
            accepted: (resp) => {
                this.spawnHandler.handle(resp);
                done.accepted();
            },
            error: (resp) => {
                done.error(resp.message);
            }
        }, character.name);
    }

    onScriptShutdown() {
        if (game) {
            game.shutdown();
            inputManager.shutdown();
        }
        server.leave();
    }

    init() {
        this.camera = new Camera();
        this.spawnHandler = new SpawnHandler(this.camera);
        this.movementHandler = new MovementHandler();
        this.chatHandler = new ChatHandler();

        this.fps = 0;
        setInterval(() => {
            this.fps = 0;
        }, 1000);
        this.entities = {};
        this.isPlaying = true;

        //Start the game loop
        this.loop();
    }

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

    lookup(id) {
        return this.entities[id];
    }

    ticker(callback) {
        this.app.ticker.add(callback);
    }

    loop() {
        this.stage.children.sort(this.depthCompare);

        this.fps++;

        this.stage.x = this.camera.getX();
        this.stage.y = this.camera.getY();

        //Render the stage to see the animation
        this.renderer.render(this.stage);

        //Loop this function at 60 frames per second
        if (this.isPlaying) {
            requestAnimationFrame(() => this.loop());
        }
    }
};

var game = new Game();