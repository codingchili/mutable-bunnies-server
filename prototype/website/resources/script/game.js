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
                application.gameLoaded();
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

        this.frames = 0;
        setInterval(() => {
            this.fps = this.frames;
            this.frames = 0;
        }, 1000);
        this.entities = {};
        this.isPlaying = true;

        this._initSkybox();
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

    _initSkybox() {
        this.clouds = [];
        assetLoader.load(skybox => {
            let ratio = Math.max(2048 / window.innerWidth, 1536 / window.innerHeight);
            skybox.scale.x = ratio;
            skybox.scale.y = ratio;

            this.root.addChildAt(skybox, 0);
            for (let cloud = 1; cloud <= 3; cloud++) {
                assetLoader.load(loaded => {
                    for (let i = 0; i < 2; i++) {
                        let cloud = new PIXI.Sprite(loaded.texture);
                        cloud.y = Math.random() * window.innerHeight;
                        cloud.x = -cloud.width;
                        cloud.velocity = Math.random() + 0.05;
                        this.clouds.push(cloud);
                        this.root.addChildAt(cloud, 1);
                    }
                }, `game/map/clouds/${cloud}.png`);
            }
        }, 'game/map/clouds/skybox_blue.png');
    }

    _updateSkybox() {
        for (let cloud of this.clouds) {
            cloud.x += cloud.velocity;
            if (cloud.x - cloud.width > window.innerWidth) {
                cloud.x = -cloud.width;
            }
        }
    }

    loop() {
        if (this.isPlaying) {
            this.stage.children.sort(Camera.depthCompare.bind(this));

            this.frames++;

            this.camera.update();
            this.stage.x = -this.camera.x;
            this.stage.y = -this.camera.y;
            this._updateSkybox();

            this.renderer.render(this.root);
            requestAnimationFrame(() => this.loop());
        }
    }
};

var game = new Game();