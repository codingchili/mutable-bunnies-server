// constants for isometric angles.
let COS = {
    _330: Math.cos(330),
    _150: Math.cos(150)
};

let SIN = {
    _330: Math.sin(330),
    _150: Math.sin(150)
};

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

        /* isometric compare using trigons */
        let inside = this._insideTrigon(a, b);

        // in some positions the sprites are blended together
        // the reorder point on the character seems to be at midpoint rather than feet.

        if (inside) {
            return -1;
        }

        /* plain depth compare on y-axis */
        if (a.y < b.y)
            return -1;
        else if (a.y > b.y)
            return 1;
        else
            return 0;
    }

    _insideTrigon(s, b) {
        let a = {
            x: COS._330 * b.height,
            y: SIN._330 * b.height,
        };
        let c = {
            x: COS._150 * b.width,
            y: SIN._150 * b.width,
        };

        let as_x = s.x - a.x;
        let as_y = s.y - a.y;

        let s_ab = (b.x - a.x) * as_y - (b.y - a.y) * as_x > 0;

        if ((c.x - a.x) * as_y - (c.y - a.y) * as_x > 0 === s_ab) {
            return false;
        } else {
            return (c.x - b.x) * (s.y - b.y) - (c.y - b.y) * (s.x - b.x) > 0 === s_ab;
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
            this.stage.children.sort(this.depthCompare.bind(this));

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