/**
 * handles network communication for websockets.
 */
class Connection {

    constructor(host, port) {
        this.port = port;
        this.host = host;
        this.handlers = {};
        this.onConnectHandlers = [];

        this.ws = new WebSocket("wss://" + this.host + ":" + this.port + "/");
        this.ws.onmessage = (msg) => {
            this.onmessage(msg);
        };
        this.ws.onopen = () => {
            this.open = true;
            for (let i = 0; i < this.onConnectHandlers.length; i++) {
                this.onConnectHandlers[i]();
            }
            this.onConnectHandlers = [];
        }
        this.ws.onerror = (evt) => {
            this.onerror(evt);
        };
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

    onmessage(event) {
       let data = JSON.parse(event.data);
       let route = data.route || data.type;

       if (this.handlers[route]) {
         if (data.status === ResponseStatus.ACCEPTED) {
            this.handlers[route].accepted(data);
         } else {
            this.handlers[route].error(data);
         }
       } else {
         console.log('no handler for message: ' + event.data);
       }
    }

    onerror(event) {
        application.error('Server error: connection closed.');
    }

    onclose(event) {
        if (!event.wasClean)
            application.error('The connection to the ' + this.realm.name + ' server was lost, please retry.');
    }
}