window.Game = class Game extends Canvas {
    static ticksToSeconds(ticks) {
        return (ticks * this.TARGET_FRAME_MS) / 1000.0;
    }

    static secondsToTicks(seconds) {
        return (seconds * 1000) / this.MS_PER_FRAME;
    }

    static get MS_PER_SERVER() {
        return 16;
    }

    static get TARGET_FRAME_MS() {
        return 6.94;
    }

    static get MS_PER_FRAME() {
        return (game.fps) ? (1000 / game.fps) : 16.67;
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
                    game.display();
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

    shutdown(disconnected) {
        if (game) {
            game.isPlaying = false;

            if (!disconnected) {
                server.leave();
            }
        }
        input.shutdown();
        application.scriptShutdown();
        super.shutdown();
    }

    init() {
        this.bus = new EventBus();
        this.camera = new Camera();
        this.spawner = new SpawnHandler(this.camera);
        this.movement = new MovementHandler();
        this.chat = new ChatHandler();
        this.dialogs = new DialogHandler();
        this.spells = new Spells();
        this.texts = new TextEffects();
        this.particles = new Particles();
        this.skybox = new Skybox();
        this.inventory = new InventoryHandler();
        this.quests = new QuestHandler();
        this.fps = 60;

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

    publish(event, data) {
        this.bus.publish(event, data);
    }

    subscribe(event, callback) {
        this.bus.subscribe(event, callback);
    }

    resize() {
        super.resize();
        if (this.skybox) {
            this.skybox.resize(this._width(), this._height());
        }
    }

    setPlayer(player) {
        this.player = player;
    }

    lookup(id) {
        return this.entities[id];
    }

    getByAccount(account) {
        for (let id in this.entities) {
            let entity = this.entities[id];
            if (entity.account === account) {
                return entity;
            }
        }
        console.log('no entity found for account ' + account);
    }

    ticker(callback) {
        this.app.ticker.add(callback);
    }

    loop() {
        if (this.isPlaying) {
            let start = performance.now();
            let delta = (start - this.last) * 0.001;

            this.stage.children.sort(Camera.depthCompare.bind(this));

            this.frames++;

            this.camera.update(delta);
            this.spells.update(delta);
            this.particles.update(delta);
            this.texts.update(delta);
            this.skybox.update(delta);
            this.movement.update(delta);

            this.stage.x = -this.camera.x;
            this.stage.y = -this.camera.y;

            let render = performance.now();
            this.renderer.render(this.root);

            this.updateTime = render - start;
            this.renderTime = performance.now() - render;
            requestAnimationFrame(() => this.loop());

            this.last = start;
        }
    }

    _metrics() {
        this.fpsMetrics.text = this.fps;
    }

    _setup() {
        if (application.development.metrics) {
            this._counter(() => {
                return `fps: ${this.fps}`;
            });
            this._counter(() => {
                return `drawables: ${this.camera.drawing}`;
            });
            this._counter(() => {
                return `update: ${this.updateTime.toFixed(2)}ms.`;
            });
            this._counter(() => {
                return `render: ${this.renderTime.toFixed(2)}ms.`;
            });
        }
    }

    _counter(text) {
        this.counters = this.counters || 0;

        let counter = new PIXI.Text(text, this.texts.style());
        counter.id = this.counters++;
        counter.y = (this._height() - 128) + 16 * counter.id + 16;
        counter.layer = 100;
        counter.text = text();

        let update = setInterval(() => {
            if (game.isPlaying) {
                counter.text = text();
                counter.x = this._width() - 128;
            } else {
                clearInterval(update);
            }
        }, 500);
        this.root.addChild(counter);
    }
};

var game = new Game();