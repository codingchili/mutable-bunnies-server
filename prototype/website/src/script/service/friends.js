/**
 * Friend API.
 */

class Friends {

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

    _send(callback, route, friend) {
        this.network.rest({
            accepted: callback,
            error: (e) => {
                application.error("Failed to call friends API.");
            }
        }, route, {
            token: application.token,
            friend: friend
        });
    }
}