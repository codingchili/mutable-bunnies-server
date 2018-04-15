/**
 * Realmserver api.
 */
class RealmServer {

    constructor(realm) {
        this.realm = realm;
        this.connection = new Connection(realm.host, realm.port);
    }

    characterlist(callback) {
        this.connection.send(callback, 'character.list', {
            token: this.realm.token
        });
    }

    create(callback, className, characterName) {
        this.connection.send(callback, 'character.create', {
            className: className,
            character: characterName
        });
    }

    remove(callback, characterName) {
        this.connection.send(callback, 'character.remove', {
            character: characterName
        });
    }

    join(callback, characterName) {
        this.connection.send(callback, 'instance.join', {
            character: characterName
        });
    }

    chatmessage(callback, message) {
        this.connection.send(callback, 'chat', {
            message: message
        });
    }

    onChatMessage(handler) {
        console.log('add handler?');
        this.connection.setHandler('chat', handler);
    }

    leave() {
        this.connection.send({
            accepted: () => {
                // disconnected successfully.
            },
            error: (e) => {
                // failed to disconnect gracefully.
                application.error(e.message);
            }
        }, 'instance.leave');
    }

    close() {
        this.connection.close();
    }

    static ping(callback, realm) {
        new Network()
            .setPort(realm.port)
            .setHost(realm.host)
            .ping(callback);
    }
}