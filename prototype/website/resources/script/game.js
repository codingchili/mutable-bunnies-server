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
                super._reset();

                this.spawner.join(event);
                this.skybox.init(event.skybox);
                this.spells.init(event);

                if (!this.loaded) {
                    console.log('application loaded emit');
                    application.gameLoaded(game);
                    this.loaded = true;
                }

                done.accepted();
            },
            error: (event) => {
                done.error(event.message);
            }
        }, character.name);
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
        this.skybox = new Skybox();
        this.inventory = new Inventory();

        this.frames = 0;
        setInterval(() => {
            this.fps = this.frames;
            this.frames = 0;
        }, 1000);
        this.entities = {};
        this.clouds = [];

        this.isPlaying = true;
        this.loop();
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
            this.stage.children.sort(Camera.depthCompare.bind(this));

            this.frames++;

            this.camera.update();
            this.spells.update();
            this.stage.x = -this.camera.x;
            this.stage.y = -this.camera.y;
            this.skybox.update();

            this.renderer.render(this.root);
            requestAnimationFrame(() => this.loop());
        }
    }
};

var game = new Game();