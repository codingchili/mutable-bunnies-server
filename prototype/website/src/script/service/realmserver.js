/**
 * Realmserver api.
 */
class RealmServer {

    constructor(realm) {
        this.realm = realm;
        this.connection = new Connection(realm.host, realm.port);
    }

    characterlist(callback) {
        this.connection.send(callback, 'character.list', {token: this.realm.token});
    }

    create(callback, className, characterName) {
        this.connection.send(callback, 'character.create', {
            token: this.realm.token,
            className: className,
            character: characterName
        });
    }

    remove(callback, characterName) {
        this.connection.send(callback, 'character.remove', {
            token: this.realm.token,
            character: characterName
        });
    }

    join(callback, characterName) {
        this.connection.send(callback, 'instance.join', {
            token: this.realm.token,
            character: characterName
        });
    }

    chatmessage(callback, text) {
        this.connection.send(callback, 'chat', {
            token: this.realm.token,
            text: text
        });
    }

    onChatMessage(handler) {
        console.log('add handler?');
        this.connection.addHandler('chat', handler);
    }

    disconnect() {
        this.connection.send({
            accepted: () => {
                // disconnected successfully.
            },
            error: (e) => {
                // failed to disconnect gracefully.
                application.error(e.message);
            }
        }, 'instance.leave', {
            token: this.realm.token
        });
    }

    static ping(callback, realm) {
        new Network()
            .setPort(realm.port)
            .setHost(realm.host)
            .ping(callback);
    }
}