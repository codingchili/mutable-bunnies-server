/**
 * Handles game dialogs.
 *
 * @type {Window.DialogHandler}
 */
window.DialogHandler = class DialogHandler {

    constructor() {

        server.connection.setHandler('dialog', (event) => {
            this._onDialog(event);
            game.movement._send(0, 0);
        });

        server.connection.setHandler('talk', {
            error: (event) => {
                game.texts.chat(game.player, {text: event.message});
            }
        });
    }

    _onDialog(dialog) {
        application.dialogEvent(dialog);

        if (dialog.end) {
            input.unblock();
        } else {
            game.camera.focus(game.lookup(dialog.targetId));
            input.block();
        }
    }

    start(targetId) {
        server.connection.send('talk', {
            targetId: targetId
        });
    }

    say(lineId) {
        server.connection.send('say', {
            next: lineId
        });
    }

    end() {
        server.connection.send('end');
        input.unblock();
        game.camera.focus(game.player);
    }
};