window.Canvas = class {

    constructor() {
        this.app = new PIXI.Application({
            antialias: false,
            transparent: false,
            resolution: 1,
            backgroundColor: 0x0
        });

        this.stage = this.app.stage;
        this.stage.interactive = true;
        this.renderer = this.app.renderer;

        if (!document.getElementById('canvas')) {
            document.body.appendChild(this.renderer.view);
        }
        this.renderer.view.id = 'canvas';
        this.renderer.view.style.animation = "fadein 0.4s ease-in 1";

        PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST;
        window.onresize = () => this.resize();
        this.resize();
    }

    shutdown() {
        document.body.removeChild(this.renderer.view);
        this.app.destroy(true);
    }

    resize() {
        this.renderer.view.style.position = "absolute";
        this.renderer.view.style.display = "block";
        this.renderer.view.style.top = "0px";
        this.renderer.view.style.left = "0px";
        this.renderer.view.style.right = "0px";
        this.renderer.view.style.bottom = "0px";
        this.renderer.autoResize = true;
        this.renderer.resize(window.innerWidth, window.innerHeight);
    }
};