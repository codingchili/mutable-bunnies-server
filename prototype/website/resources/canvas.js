window.Canvas = class {

    constructor() {
        this.app = new PIXI.Application();
        this.stage = new PIXI.Container();

        this.renderer = PIXI.autoDetectRenderer(512, 512,
            {antialias: false, transparent: false, resolution: 2, backgroundColor: 0x0, view: document.canvas}
        );

        window.onresize = () => this.resize();
        this.resize();
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
};