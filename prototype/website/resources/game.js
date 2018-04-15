let game;
window.onScriptsLoaded = () => {
    game = new Game();

    server.join({
        accepted: (resp) => {
            console.log(resp);
        },
        error: (resp) => {
            application.error(resp.message);
        }
    }, character.name);
}

window.onScriptShutdown = () => {
    if (game) {
        game.shutdown();
    }
    server.leave();
}

class Game extends Canvas {

    depthCompare(a, b) {
            if (a.layer !== b.layer) {
                if ((a.layer > b.layer))
                    return 1;
                else
                    return -1;
            }
            let yB = b.y + b.height;
            let yA = a.y + a.height;

            if (yA < yB)
                return -1;
            else if (yA > yB)
                return 1;
            else
                return 0;
        }

    shutdown() {
        super.shutdown();
        this.isPlaying = false;
    }

     hitText(x, y, text, color) {
                let style = new PIXI.TextStyle({
                    fontFamily: 'Verdana',
                    fontSize: 12,
                    //fontStyle: 'italic',
                    fontWeight: 'bold',
                    fill: [color.begin, color.end],
                    stroke: '#000000',
                    strokeThickness: 3,
                    /*dropShadow: true,
                    dropShadowColor: color.shadow,
                    dropShadowBlur: 4,
                   dropShadowAngle: Math.PI / 6,
                   dropShadowDistance: 2,*/
                    wordWrap: true,
                    wordWrapWidth: 440
                });

                let counter = new PIXI.Text(text, style);
                counter.dir = (6.14 / 360) * Math.random() * 360;

                if (Math.trunc(Math.random() * 6) === 0) {
                    style.fontSize = 16;
                    counter.ttl = 82;
                } else {
                    counter.ttl = 40;
                }


                counter.offset = this.chr.width / 2;
                counter.x = x - (counter.width / 2) + (this.chr.width / 2) + counter.offset * Math.cos(counter.dir);
                counter.y = y + (this.chr.height / 2) - (counter.height / 2) + counter.offset * Math.sin(counter.dir);
                counter.alpha = 0.0;
                counter.speed = 2.56;
                counter.ttl = 100;
                counter.slowdown = 0.925;
                counter.fade_in = 0.02;
                counter.fade_out = 0.015;
                counter.layer = 5;

                if (text[0] === '+') {
                    counter.dir = (6.14 / 360) * 270;
                    counter.speed = 1.8;
                    counter.x =  x + (this.chr.width/ 2) - (counter.width / 2);
                    counter.y = y + (counter.height / 2);
                }

                this.counters.push(counter);
                this.stage.addChild(counter);
            }

    constructor() {
        super();

        this.isPlaying = true;
        this.counters = [];
        this.chr = {x:500, y:500};
        let chr = this.chr;

        PIXI.loader
            .pre((res, next) => {
                // load files that has been loaded by the patcher.
                if (patch.files[res.url] != undefined) {
                    res.xhr = patch.files[res.url].xhr;
                    res.xhrType = 'blob';
                    res.data = patch.files[res.url].data;
                    res.complete();
                } else {
                    // if not loaded by patcher perform on-demand xhr load.
                    res.url = "/resources/" + res.url;
                }
                next();
            })
            .add("chr.png")
            .add("chr0.png")
            .add("chr1.png")
            .add("tree.png")
            .add("background_snow.png")
            .load(() => {
                let ground = new PIXI.Sprite(PIXI.loader.resources["background_snow.png"].texture);
                ground.x = 50;
                ground.y = -100;
                ground.layer = -1;
                this.stage.addChild(ground);

                this.chr = new PIXI.Sprite(PIXI.loader.resources["chr.png"].texture);
                this.chr.x = 480;
                this.chr.y = 420;
                this.chr.layer = 0;
                this.chr.scale.x = 0.5;
                this.chr.scale.y = 0.5;

                this.stage.addChild(this.chr);

                let chr0 = new PIXI.Sprite(PIXI.loader.resources["chr0.png"].texture);
                chr0.x = 400;
                chr0.y = 200;
                chr0.layer = 0;
                chr0.scale.x = 0.16;
                chr0.scale.y = 0.16;
                this.stage.addChild(chr0);


                let chr1 = new PIXI.Sprite(PIXI.loader.resources["chr1.png"].texture);
                chr1.x = 470;
                chr1.y = 240;
                chr1.layer = 0;
                chr1.scale.x = 0.16;
                chr1.scale.y = 0.16;
                this.stage.addChild(chr1);


                let tree = new PIXI.Sprite(PIXI.loader.resources["tree.png"].texture);
                tree.x = 300;
                tree.y = 275;
                tree.scale.x = 0.2;
                tree.scale.y = 0.2;
                tree.layer = 0;
                this.stage.addChild(tree);

                let tree2 = new PIXI.Sprite(PIXI.loader.resources["tree.png"].texture);
                tree2.x = 375;
                tree2.y = 300;
                tree2.layer = 0;
                tree2.scale.x = 0.3;
                tree2.scale.y = 0.3;
                this.stage.addChild(tree2);

              this.app.ticker.add(() => {
                    if (this.keys[37])
                        this.chr.x -= 1;
                    if (this.keys[38])
                        this.chr.y -= 1;
                    if (this.keys[39])
                        this.chr.x += 1;
                    if (this.keys[40])
                        this.chr.y += 1;
                });
            });


        // poison
        setInterval(() => {
            this.hitText(this.chr.x, this.chr.y, '-' + Math.trunc(Math.random() * 100 + 9), {
                begin: '#ffcc00',
                end: '#0bb001',
                stroke: '#4a1850',
                shadow: '#000000'
            });
        }, 200);

        // magic
        setInterval(() => {
            this.hitText(this.chr.x, this.chr.y, '-' + Math.trunc(Math.random() * 3000 + 9), {
                begin: '#ff03f5',
                end: '#ff00cf',
                stroke: '#4a1850',
                shadow: '#000000'
            });
        }, 1250);

        // phys
        setInterval(() => {
            this.hitText(this.chr.x, this.chr.y, '-' + Math.trunc(Math.random() * 800 + 9), {
                begin: '#ff1800',
                end: '#ff0f00',
                stroke: '#4a1850',
                shadow: '#000000'
            });
        }, 600);

        // heal
        setInterval(() => {
            this.hitText(this.chr.x, this.chr.y, '+' + Math.trunc(Math.random() * 500 + 100), {
                begin: '#06ff00',
                end: '#13ff01',
                stroke: '#4a1850',
                shadow: '#000000'
            });
        }, 3000);

        // exp
        setInterval(() => {
            this.hitText(this.chr.x, this.chr.y, '+' + Math.trunc(Math.random() * 400 + 100), {
                begin: '#ffc200',
                end: '#ffc200',
                stroke: '#4a1850',
                shadow: '#000000'
            });
        }, 800);

        // tru
        setInterval(() => {
            this.hitText(this.chr.x, this.chr.y, '-' + Math.trunc(Math.random() * 300 + 9), {
                begin: '#ffeaf9',
                end: '#ff0702',
                stroke: '#4a1850',
                shadow: '#000000'
            });
        }, 2500);

        // chat
        setInterval(() => {
            this.hitText(this.chr.x, this.chr.y, '+Hello my name is robba+', {
                begin: '#ffd8f7',
                end: '#ffe6eb',
                stroke: '#00f8ff',
                shadow: '#000000'
            });
        }, 10000);

        //Start the game loop
        this.loop();
    }

    loop() {
            this.stage.children.sort(this.depthCompare);

            for (let i = 0; i < this.counters.length; i++) {
                let counter = this.counters[i];
                counter.ttl--;

                if (counter.ttl <= 0 && counter.alpha <= 0) {
                    this.stage.removeChild(counter);
                    this.counters.splice(i, 1);
                } else {
                    counter.speed *= counter.slowdown;
                    counter.x += counter.speed * Math.cos(counter.dir);
                    counter.y += counter.speed * Math.sin(counter.dir);

                    if (counter.ttl > 0) {
                        counter.alpha += counter.fade_in;
                    }

                    if (counter.ttl < 0) {
                        counter.alpha -= counter.fade_out;
                    }

                    if (counter.alpha > 1.0) {
                        counter.alpha = 1.0;
                    }
                    if (counter.alpha < 0.0) {
                        counter.alpha = 0.0;
                    }
                }
            }

            //Render the stage to see the animation
            this.renderer.render(this.stage);

            //Loop this function at 60 frames per second
            if (this.isPlaying) {
                requestAnimationFrame(() => this.loop());
            }
        }
}