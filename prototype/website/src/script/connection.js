/**
 * handles network communication for websockets.
 */
class Connection {

    constructor(host, port, secure) {
        this.clientClosed = false;
        this.binaryWebsocket = false;
        this.port = port;
        this.host = host;
        this.handlers = {};
        this.onConnectHandlers = [];

        this.protocol = (secure) ? "wss://" : "ws://";

        this.ws = new WebSocket(this.protocol + this.host + ":" + this.port + "/");
        this.ws.binaryType = 'arraybuffer';

        this.decoder = new TextDecoder("UTF-8");
        this.ws.onmessage = (event) => {

            if (this.binaryWebsocket) {
                event.data = this.decoder.decode(event.data)
            }

            this.onmessage(event.data);
        };
        this.ws.onopen = () => {
            this.open = true;
            for (let i = 0; i < this.onConnectHandlers.length; i++) {
                this.onConnectHandlers[i]();
            }
            this.onConnectHandlers = [];
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

    send(callback, route, data) {
        data = data || {};
        data.route = route;

        this.setHandler(route, callback);

        if (this.open) {
            this.ws.send(JSON.stringify(data));
        } else {
            this.onConnected(() => this.send(callback, route, data));
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
        application.error('Server error: connection closed.');
    }

    onclose(event) {
        if (!event.wasClean)
            application.error('The connection to the ' + this.realm.name + ' server was lost, please retry.');
    }
}