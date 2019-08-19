/**
 * handles network communication for websockets.
 */
const PING_INTERVAL = 12000;

class Connection {

    constructor(realm) {
        this.clientClosed = false;
        this.binaryWebsocket = true;
        this.port = realm.port;
        this.host = realm.host;
        this.binaryWebsocket = realm.binaryWebsocket;
        this.handlers = {};
        this.onConnectHandlers = [];

        this.protocol = (realm.secure) ? "wss://" : "ws://";

        this.ws = new WebSocket(this.protocol + this.host + ":" + this.port + "/");
        this.ws.binaryType = 'arraybuffer';

        this.decoder = new TextDecoder("UTF-8");
        this.ws.onmessage = (event) => {
            let data = (this.binaryWebsocket) ?
                this.decoder.decode(event.data) :
                event.data;

            this.onmessage(data);
        };
        this.ws.onopen = () => {
            this.open = true;
            for (let i = 0; i < this.onConnectHandlers.length; i++) {
                this.onConnectHandlers[i]();
            }
            this.onConnectHandlers = [];

            this.ping = setInterval(() => {
                if (this.open && this.ws.readyState === this.ws.OPEN) {
                    this.send('ping', {}, () => {
                    });
                } else {
                    clearInterval(this.ping);
                }
            }, PING_INTERVAL);
        };
        this.ws.onerror = (evt) => {
            this.onerror(evt);
        };
    }

    onmessage(data) {
        data = JSON.parse(data);
        let route = data.route;

        if (this.handlers[route]) {
            if (data.status === ResponseStatus.ACCEPTED) {
                this.handlers[route].accepted(data);
            } else {
                this.handlers[route].error(data);
            }
        } else {
            console.log('no handler for message: ' + JSON.stringify(data));
        }
    }

    onConnected(connected) {
        this.onConnectHandlers.push(connected);
    }

    send(route, data, callback) {
        data = data || {};
        data.route = route;

        if (callback) {
            this.setHandler(route, callback);
        }

        if (this.open) {
            if (this.ws.readyState === this.ws.OPEN) {
                this.ws.send(JSON.stringify(data));
            } else {
                this.onerror();
            }
        } else {
            if (!this.clientClosed) {
                this.onConnected(() => this.send(route, data, callback));
            }
        }
    }

    close() {
        this.clientClosed = true;
        this.ws.close();
    }

    setHandler(route, callback) {
        if (!callback.accepted) {
            callback.accepted = (message) => callback(message);
        }
        if (!callback.error) {
            callback.error = (err) => application.onError(err.message);
        }
        this.handlers[route] = callback;
    }

    onerror(event) {
        this.open = false;
        application.error('Server error: connection closed.', true);
    }

    onclose(event) {
        this.open = false;
        if (!event.wasClean) {
            application.error(`The connection to the ${this.realm.name} ' +
                'server was lost, please retry.`);
        }
    }
}