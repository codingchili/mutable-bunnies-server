window.ChatHandler = class ChatHandler {

    constructor() {
        this.callbacks = [];
        this.color1 = "";
        this.color2 = "";

        server.connection.setHandler('chat', (msg) => this._onChatMessage(msg));
        server.connection.setHandler('party_chat', (msg) => {
            let sender = game.getByAccount(msg.source);
            if (sender) {
                msg.source = sender.id;
            }
            this._onChatMessage(msg)
        });

        this.onChatMessage((msg) => {
            if (msg.text && msg.target) {
                let target = game.lookup(msg.source);

                if (target) {
                    game.texts.chat(target, this._parseColors(msg));
                }
            }
        });
    }

    send(msg) {
        if (msg.startsWith("/color")) {
            let colors = msg.split(" ");
            this.color1 = "";
            this.color2 = "";

            if (colors.length > 1) {
                this.color1 = colors[1];
            }
            if (colors.length > 2) {
                this.color2 = colors[2];
            }
        } else {
            msg = `${this.color1}${this.color2}${msg}`;
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
        if (msg.party) {
            msg.color1 = '#2bc7ff';
        } else {
            let colors = /(#[0-9a-z]{6})/mgi;
            msg.color1 = colors.exec(msg.text);
            msg.color2 = colors.exec(msg.text);

            if (msg.color1) {
                msg.color1 = msg.color1[0];
            }
            if (msg.color2) {
                msg.color2 = msg.color2[0];
            }
            msg.text = msg.text.replace(/(#[0-9a-z]{6})+/mgi, "");
        }
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