window.Canvas = class {
    constructor() {
        const WIDTH = window.innerWidth;
        const HEIGHT = window.innerHeight;

        this.app = new PIXI.Application();
        this.renderer = PIXI.autoDetectRenderer(256, 256,
            {antialias: false, transparent: false, resolution: 1, backgroundColor: 0x0, view: document.canvas}
        );

        window.onresize = () => this.resize();
        this.resize();

        this.stage = new PIXI.Container();

        this.keys = [];
        document.body.addEventListener('keydown', (e) => {
            this.keys[e.keyCode] = true;
        });

        document.body.addEventListener('keyup', (e) => {
            this.keys[e.keyCode] = false;
        });
    }

    shutdown() {
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
}