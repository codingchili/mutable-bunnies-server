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

        server.join({
            accepted: (event) => {
                this.spawner.join(event);
                done.accepted();
            },
            error: (event) => {
                done.error(event.message);
            }
        }, character.name);

        application.gameLoaded();
    }

    scriptShutdown() {
        if (game) {
            game.shutdown();
            input.shutdown();
        }
        if (game.lookup(application.character.id)) {
            server.leave();
        }
    }

    init() {
        this.camera = new Camera();
        this.spawner = new SpawnHandler(this.camera);
        this.movement = new MovementHandler();
        this.chat = new ChatHandler();
        this.dialogs = new DialogHandler();
        this.spells = new Spells();
        this.texts = new TextEffects();
        this.particles = new Particles();

        this.frames = 0;
        setInterval(() => {
            this.fps = this.frames;
            this.frames = 0;
        }, 1000);
        this.entities = {};
        this.isPlaying = true;

        this.loop();
    }

    static depthCompare(a, b) {
        if (a.layer !== b.layer) {
            if ((a.layer > b.layer)) {
                return 1;
            } else {
                return -1;
            }
        }
        if (a.y === b.y) {
            return (a.x < b.x) ? 1 : -1;
        } else {
            return (a.y < b.y) ? -1 : 1;
        }
    }

    setPlayer(player) {
        this.player = player;
    }

    shutdown() {
        this.isPlaying = false;
        super.shutdown();
    }

    lookup(id) {
        return this.entities[id];
    }

    ticker(callback) {
        this.app.ticker.add(callback);
    }

    loop() {
        if (this.isPlaying) {
            this.stage.children.sort(Game.depthCompare.bind(this));

            this.frames++;

            this.camera.update();
            this.stage.x = -this.camera.x;
            this.stage.y = -this.camera.y;

            this.renderer.render(this.stage);
            requestAnimationFrame(() => this.loop());
        }
    }
};

var game = new Game();