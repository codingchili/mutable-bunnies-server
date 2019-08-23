/**
 * Friend API.
 */

class Social {

    constructor() {
        this.network = new Network('social.node');
    }

    list(callback) {
        this._send(callback, 'friend_list');
    }

    pending(callback) {
        this._send(callback, 'friend_pending');
    }

    request(callback, friend) {
        this._send(callback, 'friend_request', friend);
    }

    accept(callback, friend) {
        this._send(callback, 'friend_accept', friend);
    }

    reject(callback, friend) {
        this._send(callback, 'friend_reject', friend);
    }

    remove(callback, friend) {
        this._send(callback, 'friend_remove', friend);
    }

    suggestion(callback, friend) {
        this._send(callback, 'friend_suggest', friend);
    }

    message(callback, friend, message) {
        this._send(callback, 'friend_message', friend, message);
    }

    _send(callback, route, friend, message) {
        this.network.rest({
            accepted: callback,
            error: (e) => {
                application.publish('notification', e.message);
            }
        }, route, {
            token: application.token,
            friend: friend,
            message: message
        });
    }
}