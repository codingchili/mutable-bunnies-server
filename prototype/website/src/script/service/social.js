/**
 * Friend API.
 */

window.Social = class Social {

    constructor() {
        this.network = new Network('social.node');
    }

    friend_list(callback) {
        this._send(callback, 'friend_list');
    }

    friend_pending(callback) {
        this._send(callback, 'friend_pending');
    }

    friend_request(callback, friend) {
        this._send(callback, 'friend_request', {friend: friend});
    }

    friend_accept(callback, friend) {
        this._send(callback, 'friend_accept', {friend: friend});
    }

    friend_reject(callback, friend) {
        this._send(callback, 'friend_reject', {friend: friend});
    }

    friend_remove(callback, friend) {
        this._send(callback, 'friend_remove', {friend: friend});
    }

    friend_suggestion(callback, friend) {
        this._send(callback, 'friend_suggest', {friend: friend});
    }

    friend_message(callback, friend, message) {
        this._send(callback, 'friend_message', {friend: friend, message: message});
    }

    party_invite(callback, friend) {
        this._send(callback, 'party_invite', {friend: friend});
    }

    party_leave(callback) {
        this._send(callback, 'party_leave');
    }

    party_accept(callback, party) {
        this._send(callback, 'party_accept', {party: party});
    }

    party_decline(callback, party) {
        this._send(callback, 'party_decline', {party: party});
    }

    party_message(callback, message) {
        this._send(callback, 'party_message', {message: message});
    }

    party_list(callback) {
        this._send(callback, 'party_list');
    }

    _send(callback, route, msg) {
        this.network.rest({
                accepted: callback,
                error: (e) => {
                    application.publish('notification', e.message);
                }
            }, route,
            Object.assign({
                token: application.token,
            }, msg || {}));
    }
};


var social = new Social();