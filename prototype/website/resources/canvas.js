window.Canvas = class {

    constructor() {
        this.app = new PIXI.Application();
        this.stage = this.app.stage;//new PIXI.Container();
        this.stage.interactive = true;

        this.stage.click = () => {
            console.log('wowza');
        };

        this.stage.on('pointerdown', () => {
            console.log('wowza indeed');
        });

        this.renderer = PIXI.autoDetectRenderer(512, 512,
            {antialias: false, transparent: false, resolution: 1, backgroundColor: 0x0}
        );//view: document.canvas

        if (!document.getElementById('canvas')) {
            document.body.appendChild(this.renderer.view);
        }
        this.renderer.view.id = 'canvas';
        this.renderer.view.style.animation = "fadein 1.2s ease-in 1";

        PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST;
        window.onresize = () => this.resize();
        this.resize();
    }

    shutdown() {
        this.app.destroy(true);
        document.body.removeChild(this.renderer.view);
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