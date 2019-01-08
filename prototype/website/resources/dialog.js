/**
 * Handles game dialogs.
 *
 * @type {Window.DialogHandler}
 */
window.DialogHandler = class DialogHandler {

    constructor() {
        server.connection.setHandler('dialog', (event) => this._onDialog(event));
    }

    _onDialog(dialog) {
        application.dialogEvent(dialog);

        if (dialog.end) {
            input.unblock();
        } else {
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
    }
};