window.Canvas = class {

    constructor() {
        this.app = new PIXI.Application({
            antialias: true,
            transparent: false,
            resolution: 2,
            backgroundColor: 0x0
        });

        this.screen = this.app.screen;
        this.stage = this.app.stage;
        this.stage.interactive = true;
        this.renderer = this.app.renderer;

        if (!document.getElementById('canvas')) {
            document.body.appendChild(this.renderer.view);
        }
        this.renderer.view.id = 'canvas';
        this.renderer.view.style.animation = "fadein 0.4s ease-in 1";

        PIXI.settings.TARGET_FPMS = 0.12;
        PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST;

        window.onresize = () => this.resize();
        window.onmousedown = (e) => {
            console.log(`mouse: ${e.pageX + game.camera.x}  ${e.pageY + game.camera.y}`);
            console.log(`player: ${game.player.x} ${game.player.y}`)
        };
        this.resize();
    }

    shutdown() {
        try {
            window.onresize = () => {};
            document.body.removeChild(this.renderer.view);
        } catch (e) {
            console.log(e);
        }
        this.app.destroy(true);
    }

    resize() {
        this.renderer.view.style.position = "absolute";
        this.renderer.view.style.display = "block";
        this.renderer.view.style.top = "0px";
        this.renderer.view.style.left = "0px";
        this.renderer.view.style.right = "0px";
        this.renderer.view.style.bottom = "0px";
        this.renderer.view.ondragstart = () => false;
        this.renderer.view.ondrop = () => false;
        this.renderer.autoResize = true;
        this.renderer.resize(window.innerWidth, window.innerHeight);
    }
};