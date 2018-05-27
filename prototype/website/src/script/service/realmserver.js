/**
 * Realmserver api.
 */
class RealmServer {

    constructor(realm) {
        this.realm = realm;
        this.connection = new Connection(realm.host, realm.port, realm.secure);
    }

    connect(callback) {
        this.connection.send(callback, 'connect', {
            token: this.realm.token
        });
    }

    characterlist(callback) {
        this.connection.send(callback, 'character.list');
    }

    afflictioninfo(callback) {
        this.connection.send(callback, 'afflictioninfo');
    }

    spellinfo(callback) {
        this.connection.send(callback, 'spellinfo');
    }

    classinfo(callback) {
        this.connection.send(callback, 'classinfo');
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
        this.connection.send(callback, 'join', {
            character: characterName
        });
    }

    cast(callback, spellName, spellTarget) {
        this.connection.send(callback, 'cast', {
            spellName: spellName,
            spellTarget: spellTarget
        });
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
        }, 'leave');
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