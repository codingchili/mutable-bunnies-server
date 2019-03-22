// constants for isometric angles.
let COS = {
    _330: Math.cos(330 * Math.PI / 180),
    _150: Math.cos(210 * Math.PI / 180)
};

let SIN = {
    _330: Math.sin(330 * Math.PI / 180),
    _150: Math.sin(210 * Math.PI / 180)
};

console.log('cos330 = ' + COS._330);

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
/*        if (a.layer !== b.layer) {
            if ((a.layer > b.layer)) {
                if ((a.isPlayer || b.isPlayer) && a.id && b.id) {
                    console.log('layer diff 1');
                }
                return 1;
            } else {
                if ((a.isPlayer || b.isPlayer) && a.id && b.id) {
                    console.log('layer diff -1');
                }
                return -1;
            }
        }*/

        // test if B is in front of A
        // test if

        //this._inFrontOf(a, b);
        if (this._inFrontOf4(b, a)) {
            // If the result is negative a is sorted before b.
            return -1;
        } else {

            if (!this._inFrontOf4(a, b)) {
                return 1;
            } else {
                return 0;
            }

            // If the result is positive b is sorted before a.
            //return 1;
        }

        /*if (this._inFrontOf(a, b)) {
            // If the result is positive b is sorted before a.
            return 1;
        } else {
            if (!this._inFrontOf(b, a)) {
                return 0;
            } else {
                return -1;
            }
        }*/


        /* else {
            if (!this._isBehindOf(a,b)) {
                return 1;
            } else {
                return 0;
            }
        }*/

        /* if (this._inFrontOf(b, a)) {
             // If the result is negative a is sorted before b.
             if (a.isPlayer || b.isPlayer) {
                 //console.log(`a.player=${a.isPlayer} return -1;`);
             }
             return -1;
         } else {
             if (this._isBehindOf(a, b)) {
                 if (a.isPlayer || b.isPlayer) {
                     //console.log(`a.player=${a.isPlayer} return 0;`);
                 }
                 // If the result is positive b is sorted before a.
                 return 1; // render a on top of b.
             } else {
                 if (a.isPlayer || b.isPlayer) {
                     //console.log(`a.player=${a.isPlayer} return 1;`);
                 }
                 return 0;
             }
             // todo: this check needs to be rewritten, the check needs to be reversed somehow.*/
        // maybe: isBehind
        /*            if (this._inFrontOf(a, b)) {
                        // If the result is positive b is sorted before a.
                        if (a.isPlayer || b.isPlayer) {
                            //console.log(`a.player=${a.isPlayer} return 1;`);
                        }
                        return 1;
                    } else {
                        // no change.
                        if (a.isPlayer || b.isPlayer) {
                            //console.log(`a.player=${a.isPlayer} return 0;`);
                        }
                        return 1;
                    }*/

        /*let a_in_b = this._insideTrigon(a, b);
        let b_in_a = this._insideTrigon(b, a);

        if (a_in_b) {
            return -1;
        } else if (!b_in_a) {
            return 1;
        } else {
            return 0;*/
        /*        if (this._drawMeLast(a, b)) {
                    return 1;

                    /!*if (a.y < b.y)
                        return -1;
                    else if (a.y > b.y)
                        return 1;
                    return 1;*!/
                } else if (!this._drawMeLast(b, a)) {
                    return -1;
                } else {
                    return 0;
                }*/

        return 0;

        /* isometric compare using trigons */
        /*let a_in_b = this._insideTrigon(a, b);
        let b_in_a = this._insideTrigon(b, a);

        if (a_in_b) {
            if ((a.isPlayer || b.isPlayer) && a.id && b.id) {
                console.log('OUTSIDE');
            }
            return -1;
        } else {
            if ((a.isPlayer || b.isPlayer) && a.id && b.id) {
                console.log('INSIDE');
            }
            return 1;
        }*/
        /*else {
                   if ((a.isPlayer || b.isPlayer) && a.id && b.id) {
                      console.log('NEITHER');
                   }
                   return 0;
               }*/

        /* plain depth compare on y-axis */
        /*        if (a.y < b.y)
                    return -1;
                else if (a.y > b.y)
                    return 1;
                return 1;
                /!*else
                    return 0;*!/*/
    }

    _inFrontOf4(p1, p2) {
        // angle in radians
        //var angleRadians = Math.atan2(p2.y - p1.y, p2.x - p1.x);

        // angle in degrees
        var angleDeg = Math.atan2(p2.y - p1.y, p2.x - p1.x) * 180 / Math.PI;
        console.log(angleDeg);

        let result = (-30 > angleDeg && angleDeg > -150);


        return result;
    }

    _isBehindOf(s, b) {
        let a = {
            x: s.x + COS._150 * 4096,
            y: s.y + SIN._150 * 4096,
        };
        let c = {
            x: s.x + COS._330 * 4096,
            y: s.y + SIN._330 * 4096,
        };

        let left = (s.x - a.x) * (b.y - a.y) - (s.y - a.y) * (b.x - a.x) > 0;
        let right = (s.x - b.x) * (c.y - b.y) - (s.y - b.y) * (c.x - b.x) > 0;

        let below = (s.y < b.y);
        let behind = (left || right || below);

        if (s.debug) {
            console.log(`left=${left} right=${right} below=${below} behind=${behind}`);
        }

        return behind;
    }

    // return true if s is in front of b and needs to be rendered LAST.
    _inFrontOf(s, b) {
        let a = {
            x: s.x + COS._150 * 4096,
            y: s.y + SIN._150 * 4096,
        };
        let c = {
            x: s.x + COS._330 * 4096,
            y: s.y + SIN._330 * 4096,
        };

        let left = (b.x - a.x) * (s.y - a.y) - (b.y - a.y) * (s.x - a.x) > 0;
        let right = (b.x - s.x) * (c.y - s.y) - (b.y - s.y) * (c.x - s.x) > 0;

        let below = (s.y > b.y);
        let front = left || right || below;

        if (s.debug) {
            console.log(`(player=${s.isPlayer ? 's' : 'b'}) (${b.x},${b.y}) left=${left} right=${right} below=${below} top=${top} infrontof=${front}`);
        }
        return front;
//        }
        // if not left and not right do y comparison


        /*  if (s.debug) {
              console.log(`point a = ${a.x}, ${a.y}`);
              console.log(`point c = ${c.x}, ${c.y}`);
              console.log('');
          }

          let as_x = s.x - a.x;
          let as_y = s.y - a.y;

          if (s.debug) {
              console.log(`as_x=${as_x} as_y=${as_y}`);
          }

          let s_ab = (b.x - a.x) * as_y - (b.y - a.y) * as_x > 0;

          let result;
          let ch = (c.x - a.x) * as_y - (c.y - a.y) * as_x > 0;
          let xa = 0;

          if (ch === s_ab) {
              result = false;
          } else {
              xa = (c.x - b.x) * (s.y - b.y) - (c.y - b.y) * (s.x - b.x) > 0;
              result = (xa === s_ab);
          }

          if (s.debug) {
              console.log(`${s.isPlayer}: s_ab=${s_ab} ch=${ch} xa=${xa} (ch=s_ab)=${ch === s_ab} (xa=s_ab)=${xa === s_ab}`);
          }

          return !result;*/
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

           /* for (let i = 1; i < children.length; i++) {
                if (children[i - 1].y < children[i].y) {
                    if (this._inFrontOf(children[i - 1], children[i])) {
                        [children[i], children[i - 1]] = [children[i - 1], children[i]];
                    }
                }
                else {
                    if (this._inFrontOf(children[i], children[i - 1])) {
                        [children[i], children[i - 1]] = [children[i - 1], children[i]];
                    }
                }
            }*/

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