window.ChatHandler = class ChatHandler {

    constructor() {
        this.callbacks = [];
        this.color1 = "";
        this.color2 = "";

        server.connection.setHandler('chat', (msg) => this._onChatMessage(msg));

        this.onChatMessage((msg) => {
            if (msg.text) {
                game.texts.chat(game.lookup(msg.source), this._parseColors(msg));
            }
        });
    }

    send(msg) {
        if (msg.startsWith("/color")) {
            let colors = msg.split(" ");
            if (colors.length === 1) {
                this.color1 = "";
                this.color2 = "";
            }
            if (colors.length > 1) {
                this.color1 = colors[1];
            }
            if (colors.length > 2) {
                this.color2 = colors[2];
            }
        } else {
            msg = this.color1 + this.color2 + msg;
            server.connection.send('chat', {
                message: msg
            }, {
                accepted: (msg) => {
                    this._onChatMessage(msg);
                }
            });
        }
    }

    add(msg) {
        this._onChatMessage(msg);
    }

    _parseColors(msg) {
        let colors = /(#[0-9a-z]*)/mgi;
        msg.color1 = colors.exec(msg.text);
        msg.color2 = colors.exec(msg.text);

        if (msg.color1) {
            msg.color1 = msg.color1[0];
        }
        if (msg.color2) {
            msg.color2 = msg.color2[0];
        }
        msg.text = msg.text.replace(/(\[#.*])/mgi, "");
        return msg;
    }

    _onChatMessage(msg) {
        for (let callback of this.callbacks) {
            callback(msg);
        }
    }

    onChatMessage(callback) {
        this.callbacks.push(callback);
    }
};